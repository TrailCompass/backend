package space.itoncek.trailcompass;

import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.post;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernatePersistenceConfiguration;
import org.hibernate.tool.schema.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.commons.utils.Base64Utils;
import space.itoncek.trailcompass.database.DatabaseCard;
import space.itoncek.trailcompass.database.DatabasePlayer;
import space.itoncek.trailcompass.database.LocationEntry;
import space.itoncek.trailcompass.database.PerformanceTrace;
import space.itoncek.trailcompass.modules.DeckManager;
import space.itoncek.trailcompass.modules.LocationManager;

import java.io.IOException;
import java.util.TreeSet;

public class TrailServer {
	private static final Logger log = LoggerFactory.getLogger(TrailServer.class);
	public final boolean dev = System.getenv("dev") != null && Boolean.parseBoolean(System.getenv("dev"));
	private final int PORT = System.getenv("PORT") == null ? 8080 : Integer.parseInt(System.getenv("PORT"));
	private final String CONNECTION_STRING = System.getenv("CONNECTION_STRING") == null ? "jdbc:postgresql://localhost:5002/TrailCompass" : System.getenv("CONNECTION_STRING");
	private final String CONNECTION_USER = System.getenv("CONNECTION_USER") == null ? "postgres" : System.getenv("CONNECTION_USER");
	private final String CONNECTION_PASSWORD = System.getenv("CONNECTION_PASSWORD") == null ? "postgres" : System.getenv("CONNECTION_PASSWORD");
	public final SessionFactory ef;
	private final Javalin app;
	public final TrailCompassHandler tch;
	public final LocationManager lm;
	public final DeckManager dm;

	public TrailServer() {
		ef = new HibernatePersistenceConfiguration("TrailCompass")
				.managedClass(PerformanceTrace.class)
				.managedClass(DatabasePlayer.class)
				.managedClass(LocationEntry.class)
				.managedClass(DatabaseCard.class)
				.jdbcPoolSize(64)
				// PostgreSQL
				.jdbcUrl(CONNECTION_STRING)
				// Credentials
				.jdbcUsername(CONNECTION_USER)
				.jdbcPassword(CONNECTION_PASSWORD)
				// Automatic schema export
				.schemaToolingAction(Action.UPDATE)
				// SQL statement logging
				.showSql(true, false, true)
				// Create a new EntityManagerFactory
				.createEntityManagerFactory();

		tch = new TrailCompassHandler(this);
		lm = new LocationManager(this);
		dm = new DeckManager(this);

		app = Javalin.create(cfg -> {
			cfg.useVirtualThreads = true;
			cfg.http.gzipOnlyCompression();
			cfg.http.generateEtags = true;
			cfg.jetty.timeoutStatus = 408;
			cfg.jetty.clientAbortStatus = 499;
			cfg.jetty.modifyServer(server -> server.setStopTimeout(5_000)); // wait 5 seconds for existing requests to finish
			cfg.requestLogger.http((ctx, executionTimeMs) -> new Thread(() -> {
				try {
					if (dev) {
						String name = Base64Utils.deserializeFromBase64(ctx.body()).getClass().getName();
						log.info("{} -> {}", name, executionTimeMs);
						ef.runInTransaction(em -> {
							PerformanceTrace pt = em.find(PerformanceTrace.class, name);
							if (pt == null) {
								pt = new PerformanceTrace();
								pt.setRequestClassName(name);
								pt.setRequestDuration(new TreeSet<>());
								em.persist(pt);
							}

							pt.getRequestDuration().add(executionTimeMs);
						});
					}
				} catch (Exception e) {
					log.error("Performance trace error", e);
				}
			}).start());
			cfg.router.caseInsensitiveRoutes = true;
			cfg.router.treatMultipleSlashesAsSingleSlash = true;
			cfg.router.ignoreTrailingSlashes = true;
			cfg.router.apiBuilder(() -> post("/", tch::handle));
		});
	}

	public static void main(String[] args) {
		TrailServer ts = new TrailServer();
		ts.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				ts.stop();
			} catch (IOException e) {
				log.error("Unable to stop the server cleanly!", e);
			}
		}));
	}

	public void start() {
		dm.resetDeck();
		app.start(PORT);
	}

	private void stop() throws IOException {
		app.stop();
	}
}