package space.itoncek.trailcompass.modules;

import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.objects.Location;

public class LocationManager {
	private final TrailServer server;

	public LocationManager(TrailServer server) {
		this.server = server;
	}

	public Location getSeekerLocation() {
		return null;
	}

	public Location getHiderLocation() {
		final Location[] loc = new Location[1];
		server.ef.runInTransaction(em -> loc[0] = em.createNamedQuery("findOldestEntry", Location.class).setParameter("id", 0).getSingleResult());
		return loc[0];
	}
}
