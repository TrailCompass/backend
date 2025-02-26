package space.itoncek.trailcompass.modules;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
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
		}

		sb.append("DB: ").append(db?"Online":"Offline").append("\n");

		boolean login = server.login.isHealthy();
		sb.append("Login: ").append(login?"Online":"Offline").append("\n");

		boolean mapserver = server.mapserver.isHealthy();
		sb.append("Login: ").append(mapserver?"Online":"Offline").append("\n");

		boolean packages = server.packageLoader.isHealthy();
		sb.append("PackageLoader: ").append(packages?"Online":"Offline").append("\n");

		ctx.status((login||mapserver||packages)?HttpStatus.OK:HttpStatus.INTERNAL_SERVER_ERROR).result(sb.toString());
	}
}
