package space.itoncek.trailcompass.gamedata.requests.radar;

import java.util.List;

import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.objects.Request;
import space.itoncek.trailcompass.objects.RequestCategory;

public class RadarCategory implements RequestCategory {
	private final TrailServer server;

	public RadarCategory(TrailServer server) {
		this.server = server;
	}
	@Override
	public String getName() {
		return "Radar";
	}

	@Override
	public String getDescription() {
		return "Are you within ___km of me?\n";
	}

	@Override
	public List<Request> getRequests() {
		return List.of(
				new GenericRadarRequest(.5,server),
				new GenericRadarRequest(1,server),
				new GenericRadarRequest(2,server),
				new GenericRadarRequest(5,server),
				new GenericRadarRequest(10,server),
				new GenericRadarRequest(15,server),
				new GenericRadarRequest(50,server),
				new GenericRadarRequest(100,server),
				new GenericRadarRequest(150,server)
		);
	}
}
