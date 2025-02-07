package space.itoncek.trailcompass.measuring;

import space.itoncek.trailcompass.Request;
import space.itoncek.trailcompass.RequestCategory;

import java.util.List;

public class MeasuringCategory implements RequestCategory {
	private final LocationSupplier ls;
	private final Logger l;
	private final FeatureLibrary flib;

	public MeasuringCategory(FeatureLibrary flib, LocationSupplier locsup, Logger l) {
		this.ls = locsup;
		this.l = l;
		this.flib = flib;
	}

	@Override
	public String getName() {
		return "Measuring";
	}

	@Override
	public String getDescription() {
		return "Compared to me, are you closer to _____?";
	}

	@Override
	public List<Request> getRequests() {
		return List.of(
			new AltitudeMeasurementRequest(ls, l),
			new PoiMeasurementRequest(flib, ls, l, PoiMeasurementRequest.RAILWAY_STATION),
			new PoiMeasurementRequest(flib, ls, l, PoiMeasurementRequest.MOUNTAIN),
			new PoiMeasurementRequest(flib, ls, l, PoiMeasurementRequest.AMUSEMENT_PARK),
			new PoiMeasurementRequest(flib, ls, l, PoiMeasurementRequest.ZOO),
			new PoiMeasurementRequest(flib, ls, l, PoiMeasurementRequest.AQUARIUM),
			new PoiMeasurementRequest(flib, ls, l, PoiMeasurementRequest.MUSEUM),
			new PoiMeasurementRequest(flib, ls, l, PoiMeasurementRequest.CINEMA),
			new PoiMeasurementRequest(flib, ls, l, PoiMeasurementRequest.HOSPITAL),
			new PoiMeasurementRequest(flib, ls, l, PoiMeasurementRequest.LIBRARY),
			new PoiMeasurementRequest(flib, ls, l, PoiMeasurementRequest.EMBASSY),
			new RelationMeasurementRequest(flib,ls,l,RelationMeasurementRequest.CZ_OBEC)
		);
	}
}
