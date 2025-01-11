package space.itoncek.trailcompass;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.modules.DBManager;
import space.itoncek.trailcompass.modules.LoginSystem;

import java.sql.SQLException;

public class TrailServer {
    private static final Logger log = LoggerFactory.getLogger(TrailServer.class);
    Javalin app;
    private final int PORT = System.getenv("PORT") == null? 8080: Integer.parseInt(System.getenv("PORT"));
    private final LoginSystem login;
    public final DBManager db;

    public TrailServer() {
        app = Javalin.create(cfg -> {
            cfg.http.gzipOnlyCompression(9);
            cfg.http.prefer405over404 = true;
            cfg.router.ignoreTrailingSlashes = true;
            cfg.router.treatMultipleSlashesAsSingleSlash = true;
            cfg.router.caseInsensitiveRoutes = true;
        });

        try {
            db = new DBManager("jdbc:mariadb://%s/".formatted(System.getenv("MARIA")),System.getenv("MARIA_USER"),System.getenv("MARIA_PASSWORD"),System.getenv("MARIA_DB"));
        } catch (SQLException e) {
            log.error("Unable to init database manager",e);
            throw new RuntimeException();
        }


        try {
            login = new LoginSystem(this);
            login.registerHandlers(app);
        } catch (SQLException e) {
            log.error("Unable to init login system");
            throw new RuntimeException();
        }
    }

    private void start() throws SQLException {
        app.start(PORT);
        db.migrate();
    }

    private void stop() throws SQLException {
        app.stop();
        db.close();
    }

    public static void main(String[] args) {
        TrailServer ts = new TrailServer();

        try {
            ts.start();
        } catch (SQLException e) {
            log.error("Unable to start the server!",e);
            throw new RuntimeException();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ts.stop();
            } catch (SQLException e) {
                log.error("Unable to stop the server cleanly!",e);
            }
        }));
    }
}