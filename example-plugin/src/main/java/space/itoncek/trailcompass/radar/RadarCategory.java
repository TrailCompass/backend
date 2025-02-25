package space.itoncek.trailcompass.radar;

import org.slf4j.Logger;
import space.itoncek.trailcompass.pkg.Request;
import space.itoncek.trailcompass.pkg.RequestCategory;
import space.itoncek.trailcompass.pkg.objects.LocationSupplier;

import java.util.List;

public class RadarCategory implements RequestCategory {
	private final LocationSupplier locsup;
	private final Logger l;

	public RadarCategory(LocationSupplier locsup, Logger l) {
		this.locsup = locsup;
		this.l = l;
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
				new GenericRadarRequest(.5,locsup,l),
				new GenericRadarRequest(1,locsup,l),
				new GenericRadarRequest(2,locsup,l),
				new GenericRadarRequest(5,locsup,l),
				new GenericRadarRequest(10,locsup,l),
				new GenericRadarRequest(15,locsup,l),
				new GenericRadarRequest(50,locsup,l),
				new GenericRadarRequest(100,locsup,l),
				new GenericRadarRequest(150,locsup,l)
		);
	}
}
