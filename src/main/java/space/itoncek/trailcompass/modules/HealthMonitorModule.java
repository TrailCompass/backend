package space.itoncek.trailcompass.modules;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.TrailServer;

import java.sql.SQLException;

public class HealthMonitorModule {
	private static final Logger log = LoggerFactory.getLogger(HealthMonitorModule.class);
	private final TrailServer server;

	public HealthMonitorModule(TrailServer server) {
		this.server = server;
	}

	public void check(@NotNull Context ctx) {
		StringBuilder sb = new StringBuilder();

		boolean db = false;
		try {
			db = server.db.isHealthy();
		} catch (SQLException e) {
			log.error("Database is not healthy!");
			db = false;
		}

		sb.append("DB: ").append(db?"✅":"❌").append("\n");

		boolean login = server.login.isHealthy();
		sb.append("Login: ").append(login?"✅":"❌").append("\n");

		boolean setup = server.setup.isHealthy();
		sb.append("Setup: ").append(setup?"✅":"❌").append("\n");
	}
}
