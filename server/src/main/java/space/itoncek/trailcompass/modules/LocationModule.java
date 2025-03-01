package space.itoncek.trailcompass.modules;

import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.objects.Location;

public class LocationModule {
	private final TrailServer server;

	public LocationModule(TrailServer server) {
		this.server = server;
	}

	public Location getHiderLocation() {
		return new Location(50.0544700, 14.2905664, 0);
	}
	public Location getSeekerLocation() {
		return new Location(50.1089592, 14.5773658, 0);
	}
}
