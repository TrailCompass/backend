package space.itoncek.trailcompass.modules;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.objects.GameState;
import space.itoncek.trailcompass.objects.User;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class GameManagerModule {

    private static final Logger log = LoggerFactory.getLogger(GameManagerModule.class);
    private final TrailServer server;

    public GameManagerModule(TrailServer server) {
        this.server = server;
    }

    public void getCurrentHider(@NotNull Context ctx) {
        if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;

		try {
            int currentHiderId = server.db.getCurrentHiderId();
            ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_PLAIN).result(currentHiderId+"");
		} catch (SQLException e) {
            log.warn("DATABASE ACCESS ERROR",e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(ContentType.TEXT_PLAIN).result("500 INTERNAL SERVER ERROR");
		}

    }

    public void setCurrentHider(@NotNull Context ctx) {
        if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;

        User u = server.login.getUser(ctx);

        if(u == null || !u.admin()) {
            ctx.status(HttpStatus.UNAUTHORIZED).contentType(ContentType.TEXT_PLAIN).result("401 Unauthorized");
            return;
        }

		try {
			if (server.db.setCurrentHider(Integer.parseInt(ctx.body()))) {
				ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_PLAIN).result("200 OK");
			} else {
				ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(ContentType.TEXT_PLAIN).result("500 INTERNAL SERVER ERROR");
			}
		} catch (SQLException e) {
            log.warn("DATABASE ACCESS ERROR",e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(ContentType.TEXT_PLAIN).result("500 INTERNAL SERVER ERROR");
		}
	}

    public void getStartTime(@NotNull Context ctx) {
        if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;

		try {
            ZonedDateTime startTime = server.db.getStartTime();
            if(startTime == null) {
                log.warn("Unable to find start time in the database!");
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(ContentType.TEXT_PLAIN).result("500 INTERNAL SERVER ERROR");
            } else ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_PLAIN).result(startTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
		} catch (SQLException e) {
            log.warn("DATABASE ACCESS ERROR",e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(ContentType.TEXT_PLAIN).result("500 INTERNAL SERVER ERROR");
		}
	}

    public void getGameState(@NotNull Context ctx) {
        if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;

		try {
            GameState gameState = server.db.getGameState();
            if(gameState == null||gameState == GameState.ERROR) {
                log.warn("Unable to find game state in the database!");
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(ContentType.TEXT_PLAIN).result("500 INTERNAL SERVER ERROR");
            } else ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_PLAIN).result(gameState.name());
		} catch (SQLException e) {
            log.warn("DATABASE ACCESS ERROR",e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(ContentType.TEXT_PLAIN).result("500 INTERNAL SERVER ERROR");
		}
	}
}
