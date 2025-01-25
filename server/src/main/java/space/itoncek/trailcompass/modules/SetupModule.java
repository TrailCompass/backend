package space.itoncek.trailcompass.modules;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.objects.*;

import java.util.List;

public class SetupModule {
	private final TrailServer server;

	public SetupModule(TrailServer server) {
		this.server = server;
	}

	public void addCurse(@NotNull Context ctx) {
		if(notCorrect(ctx)) return;

		JSONObject body = new JSONObject(ctx.body());

		if (server.db.addCurse(body.getString("title"),body.getString("description"),body.getString("casting_cost"), body.getInt("amount_in_deck"))) {
			ctx.status(HttpStatus.OK).result("ok");
		} else {
			ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("DB Access error!");
		}
	}
	public void addPowerup(@NotNull Context ctx) {
		if(notCorrect(ctx)) return;

		JSONObject body = new JSONObject(ctx.body());

		if (server.db.addPowerup(body.getString("name"),body.getString("icon"), body.getInt("amount_in_deck"))) {
			ctx.status(HttpStatus.OK).result("ok");
		} else {
			ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("DB Access error!");
		}
	}

	public void addTimeBonus(@NotNull Context ctx) {
		if(notCorrect(ctx)) return;

		JSONObject body = new JSONObject(ctx.body());

		if (server.db.addTimeBonus(body.getString("title"),body.getInt("bonus_time"), body.getInt("amount_in_deck"))) {
			ctx.status(HttpStatus.OK).result("ok");
		} else {
			ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("DB Access error!");
		}
	}

	public void listCards(@NotNull Context ctx) {
		if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return;

		List<Card> cards = server.db.listCards();

		JSONArray deck_contents = new JSONArray();

		if (cards == null) {
			ctx.status(HttpStatus.BAD_REQUEST).result("unable to find any card");
			return;
		}

		int total_cards = 0;

		for (Card card : cards) {
			deck_contents.put(card.serialize());
			switch (card.getType()) {
				case CURSE -> {
					Curse c = (Curse) card;
					total_cards += c.amount_in_deck();
				}
				case POWERUP -> {
					Powerup c = (Powerup) card;
					total_cards += c.amount_in_deck();
				}
				case TIME_BONUS -> {
					TimeBonus c = (TimeBonus) card;
					total_cards += c.amount_in_deck();
				}
			}
		}

		JSONObject deck = new JSONObject()
				.put("total_cards", total_cards)
				.put("cards",deck_contents);

		ctx.status(HttpStatus.OK).result(deck.toString(4));
	}

	public void addRequestCategory(@NotNull Context ctx) {
		if(notCorrect(ctx)) return;

		JSONObject body = new JSONObject(ctx.body());

		if (server.db.addRequestClass(body.getString("name"),body.getInt("draw_cards"), body.getInt("pick_cards"))) {
			ctx.status(HttpStatus.OK).result("ok");
		} else {
			ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("DB Access error!");
		}
	}

	public void addRequest(@NotNull Context ctx) {
		if(notCorrect(ctx)) return;

		JSONObject body = new JSONObject(ctx.body());

		if (server.db.addRequest(body.getInt("classID"), body.getString("name"), body.getString("description"), body.getString("svg_icon_url"))) {
			ctx.status(HttpStatus.OK).result("ok");
		} else {
			ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("DB Access error!");
		}
	}

	public void listRequest(@NotNull Context ctx) {

	}

	private boolean notCorrect(Context ctx) {
		if (ctx.status() == HttpStatus.UNAUTHORIZED || ctx.status() == HttpStatus.IM_A_TEAPOT) return true;

		User u = server.login.getUser(ctx);

		assert u != null;
		if (!u.admin()) {
			ctx.status(HttpStatus.UNAUTHORIZED).result("Unauthorised to add curses");
			return true;
		}
		return false;
	}

	public boolean isHealthy() {
		return true;
	}
}
