package space.itoncek.trailcompass.exchange;

import com.auth0.jwt.interfaces.DecodedJWT;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.exchange.IGameManagerExchange;
import space.itoncek.trailcompass.commons.objects.Location;
import space.itoncek.trailcompass.commons.requests.gamemgr.*;
import space.itoncek.trailcompass.commons.responses.gamemgr.CurrentHiderResponse;
import space.itoncek.trailcompass.commons.responses.gamemgr.GameStateResponse;
import space.itoncek.trailcompass.commons.responses.gamemgr.SeekerLocationResponse;
import space.itoncek.trailcompass.commons.responses.gamemgr.StartingTimeResponse;
import space.itoncek.trailcompass.commons.responses.generic.OkResponse;
import space.itoncek.trailcompass.commons.utils.BackendException;
import space.itoncek.trailcompass.database.DatabasePlayer;
import space.itoncek.trailcompass.database.LocationEntry;

import java.io.IOException;
import java.util.List;
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

        try {
            return new GameStateResponse(server.gm.getGameState());
        } catch (IOException e) {
            throw new BackendException(e);
        }
    }

	@Override
	public CurrentHiderResponse getCurrentHider(CurrentHiderRequest request) throws BackendException {
		DecodedJWT jwt = server.tch.ex.auth().getJWTToken(request);
		if (jwt == null) {
			return null;
		}

        try {
            return new CurrentHiderResponse(server.gm.getCurrentHider());
        } catch (IOException e) {
            throw new BackendException(e);
        }
    }

	@Override
	public StartingTimeResponse getStartingTime(StartingTimeRequest request) throws BackendException {
		DecodedJWT jwt = server.tch.ex.auth().getJWTToken(request);
		if (jwt == null) {
			return null;
		}

        try {
            return new StartingTimeResponse(server.gm.getStartingTime());
        } catch (IOException e) {
            throw new BackendException(e);
        }
    }

	@Override
	public SeekerLocationResponse getSeekerLocation(SeekerLocationRequest request) throws BackendException {
		DecodedJWT jwt = server.tch.ex.auth().getJWTToken(request);
		if (jwt == null) {
			return null;
		}
		var ref = new Object() {
			List<Location> list;
		};
		server.ef.runInTransaction(em -> {
			ref.list = em.createNamedQuery("findAllPlayers", DatabasePlayer.class)
					.getResultList()
					.stream()
					.filter(x -> {
						try {
							return !server.config.getConfig().getHider().equals(x.getId());
						} catch (IOException e) {
							return false;
						}
					}).map(x -> em.createNamedQuery("findNewestLocation", LocationEntry.class)
							.setParameter("id", x.getId())
							.getSingleResult())
					.map(LocationEntry::serialize)
					.toList();
		});

		return new SeekerLocationResponse(ref.list);
	}
}
