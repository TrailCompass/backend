package space.itoncek.trailcompass.radar;

import org.slf4j.Logger;
import space.itoncek.trailcompass.Request;
import space.itoncek.trailcompass.RequestCategory;
import space.itoncek.trailcompass.objects.LocationSupplier;

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
		return "Are you within ?km of me?\n";
	}

	@Override
	public List<Request> getRequests() {
		return List.of(
				new GenericRadar(.5,locsup,l),
				new GenericRadar(1,locsup,l),
				new GenericRadar(2,locsup,l),
				new GenericRadar(5,locsup,l),
				new GenericRadar(10,locsup,l),
				new GenericRadar(15,locsup,l),
				new GenericRadar(50,locsup,l),
				new GenericRadar(100,locsup,l),
				new GenericRadar(150,locsup,l)
		);
	}
}
