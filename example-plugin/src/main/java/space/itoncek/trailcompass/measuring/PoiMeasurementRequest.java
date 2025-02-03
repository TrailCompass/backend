package space.itoncek.trailcompass.measuring;

import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import org.slf4j.Logger;
import space.itoncek.trailcompass.Request;
import space.itoncek.trailcompass.objects.Location;
import space.itoncek.trailcompass.objects.LocationSupplier;
import space.itoncek.trailcompass.objects.Type;
import space.itoncek.trailcompass.utils.LocationUtils;

import java.util.Arrays;
import java.util.Optional;

public class PoiMeasurementRequest implements Request {
	private final FeatureLibrary flib;
	private final LocationSupplier ls;
	private final Logger l;
	private final String query;

	public PoiMeasurementRequest(FeatureLibrary flib, LocationSupplier ls, Logger l, String query) {
		this.flib = flib;
		this.ls = ls;
		this.l = l;
		this.query = query;
	}
	@Override
	public String getName() {
		return "";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public Type getRequestType() {
		return Type.BOOLEAN;
	}

	@Override
	public Optional<Boolean> predictBool() {
		try {
			Location hider = ls.getHiderLocation().call();
			Location seeker = ls.getSeekerLocation().call();
			Optional<Double> hiderDistance = Arrays.stream(flib.nodes(query).toArray(new Feature[0]))
					.parallel()
					.map(x -> {
						Location l = new Location(x.lat(), x.lon(), 0);
						return LocationUtils.calculateDistance(hider, l);
					})
					.sorted()
					.findFirst();
			Optional<Double> seekerDistance = Arrays.stream(flib.nodes(query).toArray(new Feature[0]))
					.parallel()
					.map(x -> {
						Location l = new Location(x.lat(), x.lon(), 0);
						return LocationUtils.calculateDistance(seeker, l);
					})
					.sorted()
					.findFirst();

			if(hiderDistance.isEmpty() || seekerDistance.isEmpty()) {
				l.error("Unable to parse one or more players");
				return Optional.empty();
			}
			l.info("Hider distance: {}m", hiderDistance.get()*1000);
			l.info("Seeker distance: {}m", seekerDistance.get()*1000);
			return Optional.of(seekerDistance.get() > hiderDistance.get());
		} catch (Exception e) {
			l.error("Unable to get hider location!", e);
		}
		return Request.super.predictBool();
	}
	//Commercial airport
	//High speed train line
	public static final String RAILWAY_STATION = "[railway=station]";

	//International border
	//1st admin border (state)
	//2nd admin border (county)
	//4th admin border (borough)

	//sea level (special impl)
	//body of water
	//coastline
	public static final String MOUNTAIN = "[natural=peak]";
	//park

	public static final String AMUSEMENT_PARK = "[tourism=theme_park]";
	public static final String ZOO = "[tourism=zoo]";
	public static final String AQUARIUM = "[tourism=aquarium]";
	//golf course
	public static final String MUSEUM = "[tourism=museum]";
	public static final String CINEMA = "[amenity=cinema]";

	public static final String HOSPITAL = "[amenity=hospital]";
	public static final String LIBRARY = "[amenity=library]";
	public static final String EMBASSY = "[diplomatic=embassy]";
}
