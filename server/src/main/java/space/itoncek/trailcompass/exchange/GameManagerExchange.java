package space.itoncek.trailcompass.exchange;

import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.exchange.IGameManagerExchange;
import space.itoncek.trailcompass.commons.requests.gamemgr.*;
import space.itoncek.trailcompass.commons.responses.gamemgr.CurrentHiderResponse;
import space.itoncek.trailcompass.commons.responses.gamemgr.GameStateResponse;
import space.itoncek.trailcompass.commons.responses.gamemgr.StartingTimeResponse;
import space.itoncek.trailcompass.commons.responses.generic.OkResponse;
import space.itoncek.trailcompass.commons.utils.BackendException;

public class GameManagerExchange implements IGameManagerExchange {
	public GameManagerExchange(TrailServer server) {

	}

	@Override
	public GameStateResponse getGameState(GameStateRequest request) throws BackendException {
		// TODO))
		return null;
	}

	@Override
	public CurrentHiderResponse getCurrentHider(CurrentHiderRequest request) throws BackendException {
		// TODO))
		return null;
	}

	@Override
	public OkResponse changeCurrentHider(ChangeCurrentHiderRequest request) throws BackendException {
		// TODO))
		return null;
	}

	@Override
	public OkResponse cycleCurrentHider(CycleCurrentHiderRequest request) throws BackendException {
		// TODO))
		return null;
	}

	@Override
	public StartingTimeResponse getStartingTime(StartingTimeRequest request) throws BackendException {
		// TODO))
		return null;
	}

	@Override
	public OkResponse finishSetup(FinishSetupRequest request) throws BackendException {
		// TODO))
		return null;
	}
}
