package space.itoncek.trailcompass.radar;

import org.slf4j.Logger;
import space.itoncek.trailcompass.Request;
import space.itoncek.trailcompass.objects.Location;
import space.itoncek.trailcompass.objects.LocationSupplier;
import space.itoncek.trailcompass.objects.Type;
import space.itoncek.trailcompass.utils.LocationUtils;

import java.util.Optional;

public class GenericRadarRequest implements Request {
	private final double r;
	private final LocationSupplier locsup;
	private final Logger l;

	/**
	 * @param r Radius in kilometers
	 * @param locsup Location supplier
	 */
	public GenericRadarRequest(double r, LocationSupplier locsup, Logger l) {
		this.r = r;
		this.locsup = locsup;
		this.l = l;
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
	public Type getRequestType() {
		return Type.BOOLEAN;
	}

	@Override
	public Optional<Boolean> predictBool() {
		try {
			Location seeker = locsup.getSeekerLocation().call();
			Location hider = locsup.getHiderLocation().call();

			double distance = LocationUtils.calculateDistance(seeker,hider);
			return Optional.of(distance <= r);
		} catch (Exception e) {
			l.error("Unable to get location!", e);
			return Optional.empty();
		}
	}

	public double getRadius() {
		return r;
	}
}
