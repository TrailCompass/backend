package space.itoncek.trailcompass.modules;

import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.objects.GameState;
import space.itoncek.trailcompass.database.DatabasePlayer;
import space.itoncek.trailcompass.database.KeyStore;

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
		final GameState[] result = {GameState.ERROR};
		server.ef.runInTransaction(em -> {
			KeyStore ks = em.find(KeyStore.class, KeyStore.KeystoreKeys.GAME_STATE);
			result[0] = GameState.valueOf(ks.getKvalue());
		});
		return result[0];
	}

	public UUID getCurrentHider() {
		final UUID[] result = {null};
		server.ef.runInTransaction(em -> {
			KeyStore ks = em.find(KeyStore.class, KeyStore.KeystoreKeys.HIDER);
			if (ks.getKvalue().isEmpty()) {
				List<DatabasePlayer> players = em.createNamedQuery("findAllPlayers", DatabasePlayer.class).getResultList();
				ks.setKvalue(players.getFirst().getId().toString());
			}
			result[0] = UUID.fromString(ks.getKvalue());
		});
		return result[0];
	}

	public void changeCurrentHider(UUID uuid) {
		server.ef.runInTransaction(em -> {
			KeyStore ks = em.find(KeyStore.class, KeyStore.KeystoreKeys.HIDER);
			DatabasePlayer player = em.find(DatabasePlayer.class, uuid);

			ks.setKvalue(player.getId().toString());
			sendUpdateMessage();
		});
	}

	public ZonedDateTime getStartingTime() {
		final ZonedDateTime[] result = {null};
		server.ef.runInTransaction(em -> {
			KeyStore ks = em.find(KeyStore.class, KeyStore.KeystoreKeys.START_TIME);
			result[0] = ZonedDateTime.parse(ks.getKvalue());
		});
		return result[0];
	}

	public void finishSetup() {
		server.ef.runInTransaction(em -> {
			KeyStore ks = em.find(KeyStore.class, KeyStore.KeystoreKeys.GAME_STATE);
			ks.setKvalue(GameState.OUTSIDE_OF_GAME.name());
			sendUpdateMessage();
		});
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
