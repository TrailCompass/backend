package space.itoncek.trailcompass.modules.config;

import com.google.gson.annotations.Since;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.database.DatabasePlayer;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class Config implements Serializable {
	@Since(0.008)
	ZonedDateTime startTime;
	@Since(0.008)
	UUID hider;

	public static Config generateConfig(TrailServer server) {
		Config cfg = new Config();

		cfg.startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).plusHours(8);
		server.ef.runInTransaction(em -> {
			List<DatabasePlayer> players = em.createNamedQuery("findAllPlayers", DatabasePlayer.class).getResultList();
			cfg.hider = players.getFirst().getId();
		});

		return cfg;
	}
}
