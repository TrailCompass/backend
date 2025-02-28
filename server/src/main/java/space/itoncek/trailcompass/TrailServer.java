package space.itoncek.trailcompass;

import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.*;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import static org.apache.commons.codec.digest.DigestUtils.sha512Hex;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.database.DatabaseInterface;
import space.itoncek.trailcompass.database.MariaDatabaseImpl;
import space.itoncek.trailcompass.database.StorageDatabaseImpl;
import space.itoncek.trailcompass.modules.*;
import space.itoncek.trailcompass.packages.PackageLoader;
import static space.itoncek.trailcompass.utils.Randoms.generateRandomString;
import static space.itoncek.trailcompass.utils.Randoms.pickRandomStrings;
import space.itoncek.trailcompass.utils.TextGraphics;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class TrailServer {
	private static final Logger log = LoggerFactory.getLogger(TrailServer.class);
	public final boolean dev = System.getenv("dev") != null && Boolean.parseBoolean(System.getenv("dev"));
	public final LoginSystem login;
	public final DatabaseInterface db;
	public final MessageQueueModule mq;
	public final MapServer mapserver;
	public final PackageLoader packageLoader;
	public final LocationModule lm;
	public final GameManagerModule gamemanager;
	private final int PORT = System.getenv("PORT") == null ? 8080 : Integer.parseInt(System.getenv("PORT"));
	private final HealthMonitorModule healthMonitor;
	Javalin app;

	public TrailServer() {
		try {
			if(dev) db = new StorageDatabaseImpl(new File("./data/db.db"));
			else db = new MariaDatabaseImpl("jdbc:mariadb://%s/%s".formatted(System.getenv("MARIA"), System.getenv("MARIA_DB")), System.getenv("MARIA_USER"), System.getenv("MARIA_PASSWORD"));
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

		lm = new LocationModule(this);
		mq = new MessageQueueModule(this);
		mapserver = new MapServer(this);
		gamemanager = new GameManagerModule(this);

		//send to bottom!
		packageLoader = new PackageLoader(this);

		try {
			packageLoader.loadPlugins(new File("./packages/"));
		} catch (Exception e) {
			log.error("Unable to load plugins");
			throw new RuntimeException(e);
		}

		healthMonitor = new HealthMonitorModule(this);

		app = Javalin.create(cfg -> {
			cfg.http.disableCompression();
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
					get("userMeta", login::getUserMeta);
					get("amIAdmin",login::amIAdmin);
					get("myName",login::myName);
				});
				path("queue", ()-> {
					post("addMessage", mq::addMessage);
					get("getMyMessages", mq::getMessages);
				});
				path("mapserver", ()-> {
					get("getServerMapHash", mapserver::getServerMapHash);
					get("getServerMap", mapserver::getServerMap);
				});
				path("gamemanager", ()-> {
					get("currentHider", gamemanager::getCurrentHider);
					post("currentHider", gamemanager::setCurrentHider);
					get("startTime", gamemanager::getStartTime);
				});
				get("health", healthMonitor::check);
				get("/", this::getVersion);
				get("time", this::getTime);
			});
		});
	}
	@OpenApi(
			summary = "Get server time, useful for determining ping and synchronising clocks",
			operationId = "time",
			path = "/time",
			methods = HttpMethod.GET,
			tags = {"SYSTEM"},
			responses = {
					@OpenApiResponse(status = "200", content = {@OpenApiContent(mimeType = "text/plain", example = "1739379173617")})
			}
	)
	private void getTime(@NotNull Context context) {
		context.status(HttpStatus.OK).result(System.currentTimeMillis() + "");
	}

	@OpenApi(
			summary = "Get server version",
			operationId = "ver",
			path = "/",
			methods = HttpMethod.GET,
			tags = {"SYSTEM"},
			responses = {
					@OpenApiResponse(status = "200", content = {@OpenApiContent(mimeType = "text/plain", example = "vDEVELOPMENT")})
			}
	)
	private void getVersion(@NotNull Context context) {
		context.status(HttpStatus.OK).result(TrailServer.class.getPackage().getImplementationVersion() == null? "vDEVELOPMENT":TrailServer.class.getPackage().getImplementationVersion());
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
				.withSecurity(security -> security.withBearerAuth("JWT")));
	}

	private void start() throws SQLException {
		app.start(PORT);
		packageLoader.loadPlugins();
		db.migrate();
		if (dev) log.warn(TextGraphics.generateDevWarningBox());
		log.info(TextGraphics.generateIntroMural());
		if (db.needsDefaultUser()) {
			String user = pickRandomStrings();
			String password = generateRandomString(16, true, false);
			if (db.createUser(user, sha512Hex(password), true)) {
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