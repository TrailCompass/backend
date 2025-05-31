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

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import space.itoncek.trailcompass.commons.requests.auth.*;
import space.itoncek.trailcompass.commons.requests.gamemgr.*;
import space.itoncek.trailcompass.commons.requests.map.MapHashRequest;
import space.itoncek.trailcompass.commons.requests.map.MapRequest;
import space.itoncek.trailcompass.commons.requests.system.ServerTimeRequest;
import space.itoncek.trailcompass.commons.requests.system.ServerVersionRequest;
import space.itoncek.trailcompass.commons.responses.generic.ErrorResponse;
import space.itoncek.trailcompass.commons.utils.Base64Utils;
import space.itoncek.trailcompass.exchange.Exchange;

import java.io.IOException;
import java.io.Serializable;

@Slf4j
public class TrailCompassHandler {
	private final TrailServer server;
	public final Exchange ex;

	public TrailCompassHandler(TrailServer server) {
		this.server = server;
		this.ex = new Exchange(this.server);
	}

	public void handle(@NotNull Context ctx) throws IOException, ClassNotFoundException {
		Serializable req = Base64Utils.deserializeFromBase64(ctx.body());
		Serializable res = respond(req);
		String resString = Base64Utils.serializeToBase64(res);

		ctx.status(HttpStatus.OK).contentType("java/binary").result(resString);
	}

	private Serializable respond(Serializable request) {
		try {
			return switch (request) {
				/* System */
				case ServerTimeRequest req -> ex.system().time(req);
				case ServerVersionRequest req -> ex.system().version(req);
				/* Auth */
				case LoginRequest req -> ex.auth().login(req);
				case RegisterRequest req -> ex.auth().register(req);
				case ProfileRequest req -> ex.auth().getProfile(req);
				case ProfileOtherRequest req -> ex.auth().getOtherProfile(req);
				case ListPlayersRequest req -> ex.auth().listPlayers(req);
				/* Map */
				case MapHashRequest req -> ex.map().getMapHash(req);
				case MapRequest req -> ex.map().getMap(req);
				/* GameManager */
				case CurrentHiderRequest req -> ex.gameMgr().getCurrentHider(req);
				case EndingTimeRequest req -> ex.gameMgr().getEndingTime(req);
				case GameStateRequest req -> ex.gameMgr().getGameState(req);
				case SeekerLocationRequest req -> ex.gameMgr().getSeekerLocation(req);
				case StartingTimeRequest req -> ex.gameMgr().getStartingTime(req);
				/* default */
				case null, default -> new ErrorResponse("There is no handler for that request!");
			};
		} catch (Exception e) {
			log.error("Server handler error", e);
			return new ErrorResponse("Serverside error, please check server console!\n" + e);
		}
	}
}
