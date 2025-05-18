package space.itoncek.trailcompass.exchange;

import com.auth0.jwt.interfaces.DecodedJWT;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.exchange.IGameManagerExchange;
import space.itoncek.trailcompass.commons.requests.gamemgr.*;
import space.itoncek.trailcompass.commons.responses.gamemgr.CurrentHiderResponse;
import space.itoncek.trailcompass.commons.responses.gamemgr.GameStateResponse;
import space.itoncek.trailcompass.commons.responses.gamemgr.StartingTimeResponse;
import space.itoncek.trailcompass.commons.responses.generic.OkResponse;
import space.itoncek.trailcompass.commons.utils.BackendException;
import space.itoncek.trailcompass.database.DatabasePlayer;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class GameManagerExchange implements IGameManagerExchange {
	private final TrailServer server;

	public GameManagerExchange(TrailServer server) {
		this.server = server;
	}

	@Override
	public GameStateResponse getGameState(GameStateRequest request) throws BackendException {
		DecodedJWT jwt = server.tch.ex.auth().getJWTToken(request);
		if (jwt == null) {
			return null;
		}

		return new GameStateResponse(server.gm.getGameState());
	}

	@Override
	public CurrentHiderResponse getCurrentHider(CurrentHiderRequest request) throws BackendException {
		DecodedJWT jwt = server.tch.ex.auth().getJWTToken(request);
		if (jwt == null) {
			return null;
		}

		return new CurrentHiderResponse(server.gm.getCurrentHider());
	}

	@Override
	public OkResponse changeCurrentHider(ChangeCurrentHiderRequest request) throws BackendException {
		DecodedJWT jwt = server.tch.ex.auth().getJWTToken(request);
		if (jwt == null) {
			return null;
		}
		UUID requesterId = UUID.fromString(jwt.getClaim("id").asString());
		AtomicReference<DatabasePlayer> dbp = new AtomicReference<>(null);

		server.ef.runInTransaction(em -> {
			DatabasePlayer dp = em.find(DatabasePlayer.class, requesterId);
			dbp.set(dp);
		});

		DatabasePlayer db = dbp.get();

		if(db.isAdmin()) {
			server.gm.changeCurrentHider(request.newHider());
			return new OkResponse();
		} else return null;
	}

	@Override
	public StartingTimeResponse getStartingTime(StartingTimeRequest request) throws BackendException {
		DecodedJWT jwt = server.tch.ex.auth().getJWTToken(request);
		if (jwt == null) {
			return null;
		}

		return new StartingTimeResponse(server.gm.getStartingTime());
	}

	@Override
	public OkResponse finishSetup(FinishSetupRequest request) throws BackendException {
		DecodedJWT jwt = server.tch.ex.auth().getJWTToken(request);
		if (jwt == null) {
			return null;
		}
		UUID requesterId = UUID.fromString(jwt.getClaim("id").asString());
		AtomicReference<DatabasePlayer> dbp = new AtomicReference<>(null);

		server.ef.runInTransaction(em -> {
			DatabasePlayer dp = em.find(DatabasePlayer.class, requesterId);
			dbp.set(dp);
		});

		DatabasePlayer db = dbp.get();

		if(db.isAdmin()) {
			server.gm.finishSetup();
			return new OkResponse();
		} else return null;
	}
}
