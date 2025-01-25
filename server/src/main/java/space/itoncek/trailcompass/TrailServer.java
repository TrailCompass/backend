package space.itoncek.trailcompass;

import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.database.DatabaseInterface;
import space.itoncek.trailcompass.database.MariaDatabaseImpl;
import space.itoncek.trailcompass.modules.HealthMonitorModule;
import space.itoncek.trailcompass.modules.LoginSystem;
import space.itoncek.trailcompass.modules.SetupModule;
import space.itoncek.trailcompass.packages.PackageLoader;
import static space.itoncek.trailcompass.utils.Randoms.generateRandomString;
import space.itoncek.trailcompass.utils.TextGraphics;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class TrailServer {
	private static final Logger log = LoggerFactory.getLogger(TrailServer.class);
	public final boolean dev = System.getenv("DEV") != null && Boolean.parseBoolean(System.getenv("DEV"));
	public final LoginSystem login;
	public final SetupModule setup;
	public final DatabaseInterface db;
	public final PackageLoader packageLoader;
	private final int PORT = System.getenv("PORT") == null ? 8080 : Integer.parseInt(System.getenv("PORT"));
	private final HealthMonitorModule healthMonitor;
	Javalin app;

	public TrailServer() {
		try {
			db = new MariaDatabaseImpl("jdbc:mariadb://%s/%s".formatted(System.getenv("MARIA"), System.getenv("MARIA_DB")), System.getenv("MARIA_USER"), System.getenv("MARIA_PASSWORD"));
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

		setup = new SetupModule(this);

		//send to bottom!
		packageLoader = new PackageLoader(this);
		try {
			packageLoader.loadPlugins(new File("./plugins/"));
		} catch (Exception e) {
			log.error("Unable to load plugins");
			throw new RuntimeException(e);
		}
		healthMonitor = new HealthMonitorModule(this);

		app = Javalin.create(cfg -> {
			cfg.http.gzipOnlyCompression(9);
			cfg.http.prefer405over404 = true;
			cfg.router.ignoreTrailingSlashes = true;
			cfg.router.treatMultipleSlashesAsSingleSlash = true;
			cfg.router.caseInsensitiveRoutes = true;
			cfg.showJavalinBanner = false;
			if (dev) {
				cfg.registerPlugin(new OpenApiPlugin(TrailServer::setupOpenApi));
				cfg.registerPlugin(new SwaggerPlugin());
			}
			cfg.router.apiBuilder(() -> {
				before(login::checkTokenValidity);
				path("uac", () -> {
					post("login", login::login);
					post("register", login::register);
					get("verifyLogin", login::verifyLogin);
				});
				path("setup", () -> {
					post("addCurse", setup::addCurse);
					post("addPowerup", setup::addPowerup);
					post("addTimeBonus", setup::addTimeBonus);
					get("listCards", setup::listCards);

					post("addRequestCategory", setup::addRequestCategory);
					post("addRequest", setup::addRequest);
				});
				get("health", healthMonitor::check);
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
			} catch (IOException e) {
				log.error("Unable to stop the server cleanly!", e);
			}
		}));
	}

	private static void setupOpenApi(OpenApiPluginConfiguration pluginConfig) {
		pluginConfig.withDefinitionConfiguration((version, definition) -> definition.withInfo(info -> info.setTitle("Trailserver Backend API Specification"))
				.withServer(server -> server.description("Backend server for TrailCompass")
						.url("{url}")
						.variable("url", "URL of the server", "http://localhost:8080/"))
				.withSecurity(security -> security.withBearerAuth("JWT")));
	}

	private void start() throws SQLException {
		app.start(PORT);
		packageLoader.loadPlugins();
		log.info(TextGraphics.generateIntroMural());
		db.migrate();
		if (db.needsDefaultUser()) {
			String user = generateRandomString(10, true, false);
			String password = generateRandomString(16, true, false);
			if (db.createUser(user, password, true)) {
				log.info("New user created successfully");
			} else {
				log.error("Failed to create new user!");
			}
			log.info(TextGraphics.generateLoginBox(user, password));
		}
	}

	private void stop() throws IOException {
		packageLoader.unloadPlugins();
		app.stop();
		db.close();
	}
}