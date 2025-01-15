package space.itoncek.trailcompass.modules;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.objects.Game;
import space.itoncek.trailcompass.objects.Permission;
import space.itoncek.trailcompass.objects.User;

import java.util.ArrayList;
import java.util.List;

public class GameInstanceManagerSystem {
	private final TrailServer server;

	public GameInstanceManagerSystem(TrailServer server) {
		this.server = server;
	}

	public void createGame(@NotNull Context ctx) {
		if (ctx.status().getCode() != 401) {
			JSONObject body = new JSONObject(ctx.body());

			User user = server.login.getUser(ctx);

			if(!user.hasPermission(Permission.MANAGE_GAMES)) {
				ctx.status(HttpStatus.UNAUTHORIZED).result("You don't have the MANAGE_GAMES permission!");
				return;
			}

			if(!body.keySet().contains("db_name") || !body.getString("db_name").matches("^[A-Za-z0-9]+$")) {
				ctx.status(HttpStatus.BAD_REQUEST).result("Game name must be defined and must consist of Uppercase letters, lowercase letters and numbers!");
				return;
			}

			if (server.db.createGame(user, body.getString("db_name"))) {
				ctx.status(HttpStatus.OK).result("ok");
			} else {
				ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Unable to save the game into database!");
			}
		}
	}

	public void listGames(@NotNull Context ctx) {
		if (ctx.status().getCode() != 401) {
			ArrayList<Game> games = server.db.listGames();

			if(games == null) {
				ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result(new JSONObject().put("error", "Unable to list games! Check server console for details").toString(4));
				return;
			}

			JSONArray res = new JSONArray();

			for (Game game : games) {
				res.put(new JSONObject()
						.put("id", game.id())
						.put("owner", game.creator())
						.put("db_name", game.db_name()));
			}

			ctx.status(HttpStatus.OK).result(new JSONObject().put("games", res).toString(4));
		}
	}

	public void archiveGame(@NotNull Context ctx) {
		if (ctx.status().getCode() != 401) {
			User user = server.login.getUser(ctx);
			JSONObject body = new JSONObject(ctx.body());
			if(!body.keySet().contains("game_id") || !(body.get("game_id") instanceof Integer)) {
				ctx.status(HttpStatus.BAD_REQUEST).result("You need to provide game ID!");
				return;
			}

			Game game = server.db.getGame(body.getInt("game_id"));

			if(game.creator() != user.id() || !user.hasPermission(Permission.MANAGE_GAMES)) {
				ctx.status(HttpStatus.UNAUTHORIZED).result("You can't touch this game!");
			}

			if(game.archived()) {
				ctx.status(HttpStatus.BAD_REQUEST).result("This game is already archived!");
			} else {
				if(server.db.archiveGame(game)) {
					ctx.status(HttpStatus.OK).result("ok");
				} else {
					ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Unable to archive this game!");
				}
			}
		}
	}

	public void activateGame(@NotNull Context ctx) {
		if (ctx.status().getCode() != 401) {
			User user = server.login.getUser(ctx);
			JSONObject body = new JSONObject(ctx.body());
			if(!body.keySet().contains("game_id") || !(body.get("game_id") instanceof Integer)) {
				ctx.status(HttpStatus.BAD_REQUEST).result("You need to provide game ID!");
				return;
			}

			Game game = server.db.getGame(body.getInt("game_id"));

			if(game.creator() != user.id() || !user.hasPermission(Permission.MANAGE_GAMES)) {
				ctx.status(HttpStatus.UNAUTHORIZED).result("You can't touch this game!");
			}

			if(!game.archived()) {
				ctx.status(HttpStatus.BAD_REQUEST).result("This game isn't archived!");
			} else {
				if(server.db.archiveGame(game)) {
					ctx.status(HttpStatus.OK).result("ok");
				} else {
					ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Unable to activate this game!");
				}
			}
		}
	}
}
