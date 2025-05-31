package space.itoncek.trailcompass.modules;

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

import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.objects.GameState;
import space.itoncek.trailcompass.modules.config.Config;
import space.itoncek.trailcompass.modules.config.RestPeriod;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.UUID;

import static space.itoncek.trailcompass.commons.objects.GameState.*;

public class GameManager {
	private final TrailServer server;

	public GameManager(TrailServer server) {
		this.server = server;
	}

	public GameState getGameState() throws IOException {
		Config cfg = server.config.getConfig();
		ZonedDateTime now = ZonedDateTime.now();
		if (now.isBefore(cfg.getTime().getStartTime())) {
			return WAITING_FOR_GAME;
		} else if (now.isBefore(cfg.getTime().getStartTime().plusSeconds(cfg.getRules().getMovePeriodSeconds()))) {
			return MOVE_PERIOD;
		} else {
			boolean restPeriod = false;
			for (RestPeriod x : cfg.getTime().getRestPeriods()) {
				restPeriod = restPeriod || x.isInside(now);
			}

			if (restPeriod) {
				return REST_PERIOD;
			} else {
				if (now.isAfter(cfg.getTime().getEndTime())) {
					return ENDED;
				} else {
					return INGAME;
				}
			}
		}
	}

	public UUID getCurrentHider() throws IOException {
		return server.config.getConfig().getRules().getHider();
	}

	public ZonedDateTime getStartingTime() throws IOException {
		return server.config.getConfig().getTime().getStartTime();
	}
}
