package space.itoncek.trailcompass;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import space.itoncek.trailcompass.commons.requests.auth.LoginRequest;
import space.itoncek.trailcompass.commons.requests.auth.ProfileOtherRequest;
import space.itoncek.trailcompass.commons.requests.auth.ProfileRequest;
import space.itoncek.trailcompass.commons.requests.auth.RegisterRequest;
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
		if(res == null) res = new ErrorResponse("Serverside error, please check server console!");
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
				/* default */
				case null, default -> new ErrorResponse("There is no handler for that request!");
			};
		} catch (Exception e) {
			log.error("Server handler error",e);
			return null;
		}
	}
}
