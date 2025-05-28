package space.itoncek.trailcompass.modules;

import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.objects.GameState;
import space.itoncek.trailcompass.database.DatabasePlayer;
import space.itoncek.trailcompass.database.KeyStore;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameManager {
	private final TrailServer server;

	public GameManager(TrailServer server) {
		this.server = server;
	}

	public GameState getGameState() {
		//TODO)) Remove dependence on the database, compute on the fly!
		final GameState[] result = {GameState.ERROR};
		server.ef.runInTransaction(em -> {
			KeyStore ks = em.find(KeyStore.class, KeyStore.KeystoreKeys.GAME_STATE);
			result[0] = GameState.valueOf(ks.getKvalue());
		});
		return result[0];
	}

	public UUID getCurrentHider() throws IOException {
		return server.config.getConfig().getHider();
	}

	public ZonedDateTime getStartingTime() throws IOException {
		return server.config.getConfig().getStartTime();
	}

	final List<WsContext> ctxs = new ArrayList<>();

	public void sendUpdateMessage() {
		for (WsContext ctx : ctxs) {
			if(ctx.session.isOpen()) {
				ctx.send("update");
			} else {
				ctxs.remove(ctx);
			}
		}
	}

	public void awaitWS(WsConfig wsc) {
		wsc.onConnect(ctx-> {
			ctx.session.setIdleTimeout(Duration.ofDays(365));
			ctxs.add(ctx);
		});

		wsc.onClose(ctx -> {
			if (ctxs.stream().map(WsContext::sessionId).toList().contains(ctx.sessionId())) {
				ctxs.removeIf(x -> x.sessionId().equals(ctx.sessionId()));
			}
		});
	}
}
