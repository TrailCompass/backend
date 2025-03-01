package space.itoncek.trailcompass.modules;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.gamedata.cards.TimeBonusCard;
import space.itoncek.trailcompass.gamedata.requests.measuring.MeasuringCategory;
import space.itoncek.trailcompass.gamedata.requests.radar.RadarCategory;
import space.itoncek.trailcompass.objects.Card;
import space.itoncek.trailcompass.objects.GameState;
import space.itoncek.trailcompass.objects.RequestCategory;
import space.itoncek.trailcompass.objects.User;

public class GameManagerModule {

    private static final Logger log = LoggerFactory.getLogger(GameManagerModule.class);
    private final TrailServer server;
	private final List<Card> cards = new ArrayList<>();
	private final List<RequestCategory> requests = new ArrayList<>();

    public GameManagerModule(TrailServer server) {
        this.server = server;
    }

	public void setup() {
		cards.add(new TimeBonusCard(5,25));
		cards.add(new TimeBonusCard(10,15));
		cards.add(new TimeBonusCard(15,10));
		cards.add(new TimeBonusCard(20,3));
		cards.add(new TimeBonusCard(30,2));

		requests.add(new MeasuringCategory(server.mapserver.flib,server));
		requests.add(new RadarCategory(server));
	}

    public void getCurrentHider(@NotNull Context ctx) {
        if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;

		try {
            int currentHiderId = server.db.getCurrentHiderId();
			User user = server.db.getUserByID(currentHiderId);
			if (user != null) {
				ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON).result(user.toJSON().toString(4));
			} else {
				ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(ContentType.TEXT_PLAIN).result("500 INTERNAL SERVER ERROR");
			}
		} catch (SQLException e) {
            log.warn("DATABASE ACCESS ERROR",e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(ContentType.TEXT_PLAIN).result("500 INTERNAL SERVER ERROR");
		}

    }

	public void cycleHider(@NotNull Context ctx) {
		if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;

		try {
			List<User> users = server.db.listUsers();
			assert users != null;
			int currentHiderId = server.db.getCurrentHiderId();
			int nextHiderIndex = 0;
			User user = users.stream()
					.filter(x -> x.id() == currentHiderId)
					.limit(1)
					.findFirst()
					.orElseGet(()->new User(-1,"ERROR",false,false));

			if(user.id() != -1) {
				for (int i = 0; i < users.size(); i++) {
					User u = users.get(i);
					if(u == user) {
						nextHiderIndex = i+1;
					}
				}
			}

			User selectedUser = users.get(nextHiderIndex % users.size());

			server.db.setCurrentHider(selectedUser.id());
			ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_PLAIN).result("200 OK");
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

	public void finishSetup(@NotNull Context ctx) {
		if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;
		ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_PLAIN).result("200 OK");
	}
}
