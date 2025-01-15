package space.itoncek.trailcompass;

import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.modules.DBManager;
import space.itoncek.trailcompass.modules.GameInstanceManagerSystem;
import space.itoncek.trailcompass.modules.LoginSystem;

import java.sql.SQLException;

public class TrailServer {
	private static final Logger log = LoggerFactory.getLogger(TrailServer.class);
	public final boolean dev = System.getenv("DEV") != null && Boolean.parseBoolean(System.getenv("DEV"));
	private final GameInstanceManagerSystem gims;
	public final LoginSystem login;
	public final DBManager db;
	private final int PORT = System.getenv("PORT") == null ? 8080 : Integer.parseInt(System.getenv("PORT"));
	Javalin app;

	public TrailServer() {
		try {
			db = new DBManager("jdbc:mariadb://%s/".formatted(System.getenv("MARIA")), System.getenv("MARIA_USER"), System.getenv("MARIA_PASSWORD"), System.getenv("MARIA_DB"));
		} catch (SQLException e) {
			log.error("Unable to init database manager", e);
			throw new RuntimeException();
		}

		try {
			login = new LoginSystem(this);
		} catch (SQLException e) {
			log.error("Unable to init login system");
			throw new RuntimeException();
		}
		gims = new GameInstanceManagerSystem(this);

		app = Javalin.create(cfg -> {
			cfg.http.gzipOnlyCompression(9);
			cfg.http.prefer405over404 = true;
			cfg.router.ignoreTrailingSlashes = true;
			cfg.router.treatMultipleSlashesAsSingleSlash = true;
			cfg.router.caseInsensitiveRoutes = true;
			if (dev) {
				cfg.registerPlugin(new OpenApiPlugin(pluginConfig -> {
					pluginConfig.withDefinitionConfiguration((version, definition) -> {
						definition.withInfo(info -> info.setTitle("Trailserver Backend API Specification"))
								.withServer(server -> {
									server.description("Backend server for TrailCompass")
											.url("{url}")
											.variable("url", "URL of the server", "http://localhost:8080/");
								})
								.withSecurity(security -> {
									security.withBearerAuth("JWT");
								});
					});
				}));
				cfg.registerPlugin(new SwaggerPlugin());
			}
			cfg.router.apiBuilder(() -> {
				before(login::checkTokenValidity);
				path("uac", () -> {
					post("login", login::login);
					post("register", login::register);
					get("verifyLogin", login::verifyLogin);
				});
				path("gamemanager", ()-> {
					post("createGame", gims::createGame);
					get("listGames", gims::listGames);
					post("activateGame", gims::activateGame);
					post("archiveGame", gims::archiveGame);
				});
			});
		});
	}

	public static void main(String[] args) {
		TrailServer ts = new TrailServer();

		try {
			ts.start();
		} catch (SQLException e) {
			log.error("Unable to start the server!", e);
			throw new RuntimeException();
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				ts.stop();
			} catch (SQLException e) {
				log.error("Unable to stop the server cleanly!", e);
			}
		}));
	}

	private void start() throws SQLException {
		app.start(PORT);
		db.migrate();
	}

	private void stop() throws SQLException {
		app.stop();
		db.close();
	}
}