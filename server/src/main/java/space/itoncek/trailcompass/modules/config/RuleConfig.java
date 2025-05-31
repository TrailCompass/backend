package space.itoncek.trailcompass.modules.config;

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

import lombok.Getter;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.objects.Player;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class RuleConfig implements Serializable {
    UUID hider;
    GameSize size;
    long movePeriodSeconds;
    boolean includeMoveCard;
    boolean playingWithPhysicalCards;

	public static RuleConfig generate(TrailServer server) {
		RuleConfig cfg = new RuleConfig();
		cfg.hider = server.tch.ex.auth().listPlayers().stream().findFirst().map(Player::id).orElseGet(UUID::randomUUID);
		cfg.size = GameSize.Medium;
		cfg.movePeriodSeconds = 3600;
		cfg.includeMoveCard = true;
		cfg.playingWithPhysicalCards = false;
		return cfg;
	}
}
