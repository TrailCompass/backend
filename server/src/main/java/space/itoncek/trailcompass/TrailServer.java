package space.itoncek.trailcompass;

/*
 *
 * ████████╗██████╗  █████╗ ██╗██╗      ██████╗ ██████╗ ███╗   ███╗██████╗  █████╗ ███████╗███████╗
 * ╚══██╔══╝██╔══██╗██╔══██╗██║██║     ██╔════╝██╔═══██╗████╗ ████║██╔══██╗██╔══██╗██╔════╝██╔════╝
 *    ██║   ██████╔╝███████║██║██║     ██║     ██║   ██║██╔████╔██║██████╔╝███████║███████╗███████╗
 *    ██║   ██╔══██╗██╔══██║██║██║     ██║     ██║   ██║██║╚██╔╝██║██╔═══╝ ██╔══██║╚════██║╚════██║
 *    ██║   ██║  ██║██║  ██║██║███████╗╚██████╗╚██████╔╝██║ ╚═╝ ██║██║     ██║  ██║███████║███████║
 *    ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚══════╝ ╚═════╝ ╚═════╝ ╚═╝     ╚═╝╚═╝     ╚═╝  ╚═╝╚══════╝╚══════╝
 *
 *                                    Copyright (c) 2025.
 */

import io.javalin.Javalin;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernatePersistenceConfiguration;
import org.hibernate.tool.schema.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.commons.utils.Base64Utils;
import space.itoncek.trailcompass.database.*;
import space.itoncek.trailcompass.database.cards.Card;
import space.itoncek.trailcompass.database.cards.DeckCard;
import space.itoncek.trailcompass.database.cards.ShadowCard;
import space.itoncek.trailcompass.database.curses.TextCurse;
import space.itoncek.trailcompass.database.mesages.Message;
import space.itoncek.trailcompass.gamedata.utils.TextGraphics;
import space.itoncek.trailcompass.modules.ConfigManager;
import space.itoncek.trailcompass.modules.DeckManager;
import space.itoncek.trailcompass.modules.GameManager;
import space.itoncek.trailcompass.modules.LocationManager;

import java.io.IOException;
import java.util.TreeSet;

import static io.javalin.apibuilder.ApiBuilder.post;
import static org.apache.commons.codec.digest.DigestUtils.sha512;
import static space.itoncek.trailcompass.commons.utils.RandomUtils.generateRandomString;
import static space.itoncek.trailcompass.commons.utils.RandomUtils.pickRandomStrings;

public class TrailServer {
	private static final Logger log = LoggerFactory.getLogger(TrailServer.class);
	public final boolean dev = System.getenv("dev") != null && Boolean.parseBoolean(System.getenv("dev"));
	private final int PORT = System.getenv("PORT") == null ? 8080 : Integer.parseInt(System.getenv("PORT"));
    public final SessionFactory ef;
	private final Javalin app;
	public final TrailCompassHandler tch;
	public final LocationManager lm;
	public final DeckManager dm;
	public final GameManager gm;
	public final ConfigManager config;

	public TrailServer() {
        String connString = System.getenv("CONNECTION_STRING") == null ? "jdbc:postgresql://localhost:5002/TrailCompass" : System.getenv("CONNECTION_STRING");
		String connUser = System.getenv("CONNECTION_USER") == null ? "postgres" : System.getenv("CONNECTION_USER");
		String connPassword = System.getenv("CONNECTION_PASSWORD") == null ? "postgres" : System.getenv("CONNECTION_PASSWORD");
		ef = new HibernatePersistenceConfiguration("TrailCompass")
				.managedClasses(
						PerformanceTrace.class,
						DatabasePlayer.class,
						LocationEntry.class,
						KeyStore.class,
						//cards
						Card.class,
						DeckCard.class,
						ShadowCard.class,
						//curses
						PlayedCurse.class,
						CurseMetadata.class,
						TextCurse.class,
						FreeQuestionToken.class,
						//messages
						Message.class
				)
				.jdbcPoolSize(8)
				// PostgreSQL
				.jdbcUrl(connString)
				// Credentials
				.jdbcUsername(connUser)
				.jdbcPassword(connPassword)
				// Automatic schema export
				.schemaToolingAction(Action.UPDATE)
				// SQL statement logging
				.showSql(true, false, true)
				// Create a new EntityManagerFactory
				.createEntityManagerFactory();


		tch = new TrailCompassHandler(this);
		config = new ConfigManager(this);
		lm = new LocationManager(this);
		dm = new DeckManager(this);
		gm = new GameManager(this);

		app = Javalin.create(cfg -> {
			cfg.useVirtualThreads = true;
			cfg.http.gzipOnlyCompression();
			cfg.http.generateEtags = true;
			cfg.jetty.timeoutStatus = 408;
			cfg.jetty.clientAbortStatus = 499;
			cfg.showJavalinBanner = false;
			cfg.jetty.modifyServer(server -> server.setStopTimeout(5_000)); // wait 5 seconds for existing requests to finish
			cfg.requestLogger.http((ctx, executionTimeMs) -> new Thread(() -> {
				try {
					if (dev) {
						String name = Base64Utils.deserializeFromBase64(ctx.body()).getClass().getSimpleName();
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
		for (KeyStore.KeystoreKeys v : KeyStore.KeystoreKeys.values()) {
			ef.runInTransaction(em -> {
				KeyStore ks = em.find(KeyStore.class, v);
				if (ks == null) {
					ks = new KeyStore();
					ks.setKkey(v);
					ks.setKvalue(v.defaults);
					em.persist(ks);
				}
			});
		}

		final boolean[] needsCardReset = {false};
		ef.runInTransaction(em -> {
			KeyStore ks = em.find(KeyStore.class, KeyStore.KeystoreKeys.DECK_DEALT);
			if (!Boolean.parseBoolean(ks.getKvalue())) {
				needsCardReset[0] = true;
			}
		});

		if (needsCardReset[0]) {
			dm.resetDeck();
			ef.runInTransaction(em -> {
				KeyStore ks = em.find(KeyStore.class, KeyStore.KeystoreKeys.DECK_DEALT);
				ks.setKvalue("true");
			});
		}

		app.start(PORT);
		log.info("{}\n{}", TextGraphics.generateIntroMural(), TextGraphics.isDebuggerPresent() ? TextGraphics.generateDevWarningBox() : "");

		if (tch.ex.auth().needsDefaultUser()) {
            String user;
            String password;
            if (dev) {
                user = "admin";
                password = "root";
            }else {
			user = pickRandomStrings();
			password = generateRandomString(16, true, false);
			}
            tch.ex.auth().createUser(user, sha512(password), true);
            log.info(TextGraphics.generateLoginBox(user, password));
        }
		// TODO)) Remove before release
		//  use this to test different parts of the app as it is in it's ready state at this point
		//  just set the breakpoint at the next line and wait for it to pause there.
		System.out.println("breakpoint");
	}

	private void stop() throws IOException {
		app.stop();
	}
}