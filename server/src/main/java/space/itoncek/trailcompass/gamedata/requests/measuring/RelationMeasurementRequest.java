package space.itoncek.trailcompass.gamedata.requests.measuring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.geodesk.feature.Feature;
import com.geodesk.feature.FeatureLibrary;
import com.geodesk.geom.Mercator;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.gamedata.utils.LocationUtils;
import space.itoncek.trailcompass.objects.Location;
import space.itoncek.trailcompass.objects.Request;
import space.itoncek.trailcompass.objects.gamedata.Type;

public class RelationMeasurementRequest implements Request {
	private static final Logger l = LoggerFactory.getLogger(RelationMeasurementRequest.class);
	private final FeatureLibrary flib;
	private final TrailServer server;
	private final String query;

	public RelationMeasurementRequest(FeatureLibrary flib, TrailServer server, String query) {
		this.flib = flib;
		this.server = server;
		this.query = query;
	}

	@Override
	public String getID() {
		return "space.itoncek.trailcompass.gamedata.requests.measuring.RelationMeasurementRequest."+query;
	}

	@Override
	public String getName() {
		return "Distance from " + query;
	}

	@Override
	public String getDescription() {
		return "Compared to me, are you closer to _____?";
	}

	@Override
	public Type getRequestType() {
		return Type.BOOLEAN;
	}

	@Override
	public Optional<Boolean> predictBool() {
		try {
			Location hider = server.lm.getHiderLocation();
			Location seeker = server.lm.getSeekerLocation();

			Optional<Double> hiderDistance =  getAreaDistance(hider);
			Optional<Double> seekerDistance = getAreaDistance(seeker);


			if (hiderDistance.isEmpty() || seekerDistance.isEmpty()) {
				l.error("Unable to parse one or more players");
				return Optional.empty();
			}
			l.info("Hider distance: {}m", hiderDistance.get() * 1000);
			l.info("Seeker distance: {}m", seekerDistance.get() * 1000);
			return Optional.of(seekerDistance.get() > hiderDistance.get());
		} catch (Exception e) {
			l.error("Unable to get hider location!", e);
		}
		return Request.super.predictBool();
	}

	private Optional<Double> getAreaDistance(Location target) {
		return Arrays.stream(flib.relations(query).containingLonLat(target.lon(), target.lat()).toArray(new Feature[0]))
				.parallel()
				.map(feature -> feature.toGeometry().getBoundary())
				.filter(x -> x instanceof LinearRing)
				.map(x -> (LinearRing) x)
				.map(x -> {
					ArrayList<Point> points = new ArrayList<>();
					for (int i = 0; i < x.getNumPoints(); i++) {
						points.add(x.getPointN(i));
					}
					return points;
				})
				.map(x -> {
					Optional<Double> first = x.parallelStream()
							.map(y -> {
								Location l = new Location(Mercator.latFromY(y.getY()), Mercator.lonFromX(y.getX()), 0);
								return LocationUtils.calculateDistance(target, l);
							})
							.sorted()
							.findFirst();
					return first.orElse(null);
				})
				.filter(Objects::nonNull)
				.sorted()
				.findFirst();
	}

	//Commercial airport???
	//public static final String RAILWAY_SPEED_LINE = "[railway=rail]";
	//public static final String TRAMWAY_SPEED_LINE = "[railway=tram]";
	//railway station

	//NOTE: Refer the following URL for admin level numbers https://wiki.openstreetmap.org/wiki/Tag:boundary%3Dadministrative#Country_specific_values_%E2%80%8B%E2%80%8Bof_the_key_admin_level=*
	//public static final String INTERNATIONAL_BORDER = "[boundary=administrative][admin_level=2]"; //NOT USED IN OUR GAME
	public static final String CZ_KRAJE = "[boundary=administrative][admin_level=6]";
	public static final String CZ_OKRES = "[boundary=administrative][admin_level=7]";
	public static final String CZ_OBEC = "[boundary=administrative][admin_level=8]";


	//sea level (special impl)
	//public static final String BODY_OF_WATER = "";
	//public static final String COASTLINE = "";
	//mountain
	//public static final String PARK = "";

	//amusement park
	//zoo
	//aquarium
	//public static final String GOLF_COURSE = "";
	//museum
	//cinema

	//hopital
	//library
	//embassy
}
