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

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class TimeConfig implements Serializable {
    ZonedDateTime startTime;
    List<RestPeriod> restPeriods;
    ZonedDateTime endTime;

	public static TimeConfig generate(TrailServer server) {
		TimeConfig cfg = new TimeConfig();
		cfg.startTime = ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault()).plusDays(1).plusHours(8);
		cfg.endTime = ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault()).plusDays(2).plusHours(18);
		cfg.restPeriods = new ArrayList<>();
		return cfg;
	}
}
