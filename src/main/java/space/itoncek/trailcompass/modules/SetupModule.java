package space.itoncek.trailcompass.modules;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.objects.Card;
import space.itoncek.trailcompass.objects.User;

import java.util.List;

public class SetupModule {
	private final TrailServer server;

	public SetupModule(TrailServer server) {
		this.server = server;
	}

	public void addCard(@NotNull Context ctx) {
		if (ctx.status().getCode() == 401) return;

		User u = server.login.getUser(ctx);

		if (!u.admin()) {
			ctx.status(HttpStatus.UNAUTHORIZED).result("Unauthorised to add curses");
			return;
		}

		JSONObject body = new JSONObject(ctx.body());

		Card card = Card.deserialize(body);

		if (card == null) {
			ctx.status(HttpStatus.BAD_REQUEST).result("Unable to parse a card");
			return;
		}

		if (server.db.addCard(card)) {
			ctx.status(HttpStatus.OK).result("ok");
		} else {
			ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("DB Access error!");
		}
	}

	public void listCards(@NotNull Context ctx) {
		if (ctx.status().getCode() == 401) return;

		List<Card> cards = server.db.listCards();
	}
}
