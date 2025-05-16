package space.itoncek.trailcompass.gamedata.requests.radar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.gamedata.utils.LocationUtils;
import space.itoncek.trailcompass.objects.DataType;
import space.itoncek.trailcompass.commons.objects.Location;
import space.itoncek.trailcompass.objects.Request;

import java.util.Optional;

public class GenericRadarRequest implements Request {
	private static final Logger log = LoggerFactory.getLogger(GenericRadarRequest.class);
	private final double r;
	private final TrailServer server;

	/**
	 * @param r Radius in kilometers
	 * @param server Trailcompass server
	 */
	public GenericRadarRequest(double r, TrailServer server) {
		this.r = r;
		this.server = server;
	}

	@Override
	public String getID() {
		return "space.itoncek.trailcompass.gamedata.requests.radar.GenericRadarRequest";
	}

	@Override
	public String getName() {
		return r + "km Radar";
	}

	@Override
	public String getDescription() {
		return "Are you within %f km of me?".formatted(r);
	}

	@Override
	public DataType getRequestType() {
		return DataType.BOOLEAN;
	}

	@Override
	public Optional<Boolean> predictBool() {
		try {
			Location seeker = server.lm.getSeekerLocation();
			Location hider = server.lm.getHiderLocation();

			double distance = LocationUtils.calculateDistance(seeker,hider);
			return Optional.of(distance <= r);
		} catch (Exception e) {
			log.error("Unable to get location!", e);
			return Optional.empty();
		}
	}

	public double getRadius() {
		return r;
	}
}
