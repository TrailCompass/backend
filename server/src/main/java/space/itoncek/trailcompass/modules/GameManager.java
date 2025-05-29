package space.itoncek.trailcompass.modules;

import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.objects.GameState;
import space.itoncek.trailcompass.modules.config.Config;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.UUID;

public class GameManager {
	private final TrailServer server;

	public GameManager(TrailServer server) {
		this.server = server;
	}

	public GameState getGameState() throws IOException {
		Config cfg = server.config.getConfig();
		ZonedDateTime start = cfg.getStartTime();
		if(ZonedDateTime.now().isBefore(start)) {
			return GameState.OUTSIDE_OF_GAME;
		}else if (ZonedDateTime.now().isBefore(cfg.getStartTime().plusSeconds(cfg.getMovePeriodSeconds()))) {
			return GameState.MOVE_PERIOD;
		} else if(ZonedDateTime.now().isBefore(cfg.getGameDayEndTime())) {
			return GameState.INGAME;
		} else return GameState.REST_PERIOD;
	}

	public UUID getCurrentHider() throws IOException {
		return server.config.getConfig().getHider();
	}

	public ZonedDateTime getStartingTime() throws IOException {
		return server.config.getConfig().getStartTime();
	}
}
