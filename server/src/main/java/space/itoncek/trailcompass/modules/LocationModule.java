package space.itoncek.trailcompass.modules;

import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.objects.Location;
import space.itoncek.trailcompass.objects.LocationSupplier;

public class LocationModule {
	private final TrailServer server;

	public LocationModule(TrailServer server) {
		this.server = server;
	}

	public LocationSupplier getLocationSupplier() {
		return new LocationSupplier(() -> new Location(50.0544700, 14.2905664, 0),
				() -> new Location(50.1089592, 14.5773658, 0));
	}
}
