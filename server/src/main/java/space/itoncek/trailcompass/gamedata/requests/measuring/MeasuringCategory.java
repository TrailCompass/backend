package space.itoncek.trailcompass.gamedata.requests.measuring;

import java.util.List;

import com.geodesk.feature.FeatureLibrary;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.objects.Request;
import space.itoncek.trailcompass.objects.RequestCategory;

public class MeasuringCategory implements RequestCategory {
	private final TrailServer server;
	private final FeatureLibrary flib;

	public MeasuringCategory(FeatureLibrary flib, TrailServer server) {
		this.server = server;
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
				new AltitudeMeasurementRequest(server),
				new PoiMeasurementRequest(flib, server, PoiMeasurementRequest.RAILWAY_STATION),
				new PoiMeasurementRequest(flib, server, PoiMeasurementRequest.MOUNTAIN),
				new PoiMeasurementRequest(flib, server, PoiMeasurementRequest.AMUSEMENT_PARK),
				new PoiMeasurementRequest(flib, server, PoiMeasurementRequest.ZOO),
				new PoiMeasurementRequest(flib, server, PoiMeasurementRequest.AQUARIUM),
				new PoiMeasurementRequest(flib, server, PoiMeasurementRequest.MUSEUM),
				new PoiMeasurementRequest(flib, server, PoiMeasurementRequest.CINEMA),
				new PoiMeasurementRequest(flib, server, PoiMeasurementRequest.HOSPITAL),
				new PoiMeasurementRequest(flib, server, PoiMeasurementRequest.LIBRARY),
				new PoiMeasurementRequest(flib, server, PoiMeasurementRequest.EMBASSY),
				new RelationMeasurementRequest(flib, server, RelationMeasurementRequest.CZ_OBEC)
		);
	}
}
