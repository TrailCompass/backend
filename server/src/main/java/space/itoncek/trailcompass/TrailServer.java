package space.itoncek.trailcompass;

import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.post;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernatePersistenceConfiguration;
import org.hibernate.tool.schema.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.commons.utils.Base64Utils;
import space.itoncek.trailcompass.database.PerformanceTrace;
import space.itoncek.trailcompass.database.DatabasePlayer;

import java.io.IOException;
import java.util.TreeSet;

public class TrailServer {
	private static final Logger log = LoggerFactory.getLogger(TrailServer.class);
	public final boolean dev = System.getenv("dev") != null && Boolean.parseBoolean(System.getenv("dev"));
	private final int PORT = System.getenv("PORT") == null ? 8080 : Integer.parseInt(System.getenv("PORT"));
	public final SessionFactory ef;
	private final Javalin app;
	private final TrailCompassHandler tch;

	public TrailServer() {
		ef = new HibernatePersistenceConfiguration("TrailCompass")
				.managedClass(PerformanceTrace.class)
				.managedClass(DatabasePlayer.class)
				// PostgreSQL
				.jdbcUrl("jdbc:postgresql://localhost:5002/TrailCompass")
				// Credentials
				.jdbcUsername("postgres")
				.jdbcPassword("postgres")
				// Automatic schema export
				.schemaToolingAction(Action.UPDATE)
				// SQL statement logging
				.showSql(true, false, true)
				// Create a new EntityManagerFactory
				.createEntityManagerFactory();

		tch = new TrailCompassHandler(this);

		app = Javalin.create(cfg -> {
			cfg.useVirtualThreads = true;
			cfg.http.gzipOnlyCompression();
			cfg.http.generateEtags = true;
			cfg.jetty.timeoutStatus = 408;
			cfg.jetty.clientAbortStatus = 499;
			cfg.jetty.modifyServer(server -> server.setStopTimeout(5_000)); // wait 5 seconds for existing requests to finish
			cfg.requestLogger.http((ctx, executionTimeMs) -> {
				new Thread(() -> {
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
						log.error("Performance trace error",e);
					}
				}).start();
			});
			cfg.router.caseInsensitiveRoutes = true;
			cfg.router.treatMultipleSlashesAsSingleSlash = true;
			cfg.router.ignoreTrailingSlashes = true;
			cfg.router.apiBuilder(()-> {
				post("/", tch::handle);
			});
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

	private void start() {
		app.start(PORT);
	}

	private void stop() throws IOException {
		app.stop();
	}
}