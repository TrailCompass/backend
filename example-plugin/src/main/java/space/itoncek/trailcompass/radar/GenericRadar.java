package space.itoncek.trailcompass.radar;

import org.slf4j.Logger;
import space.itoncek.trailcompass.Request;
import space.itoncek.trailcompass.objects.Location;
import space.itoncek.trailcompass.objects.LocationSupplier;

import java.util.Optional;

public class GenericRadar implements Request {
	private final double r;
	private final LocationSupplier locsup;
	private final Logger l;

	/**
	 * @param r Radius in kilometers
	 * @param locsup Location supplier
	 */
	public GenericRadar(double r, LocationSupplier locsup, Logger l) {
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
	public Optional<Boolean> executeBool() {
		try {
			Location seeker = locsup.getSeekerLocation().call();
			Location hider = locsup.getHiderLocation().call();

			double distance = calculateDistance(seeker,hider);
			return Optional.of(distance <= r);
		} catch (Exception e) {
			l.error("Unable to get location!", e);
			return Optional.empty();
		}
	}

	public static double calculateDistance(Location l1, Location l2) {
		long earthRadiusKm = 6371;

		double dLat = Math.toRadians(l2.lat()-l1.lat());
		double dLon = Math.toRadians(l2.lon()-l1.lon());

		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
				Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(Math.toRadians(l1.lat())) * Math.cos(Math.toRadians(l2.lat()));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return earthRadiusKm * c;
	}

	@Override
	public Optional<Boolean> predictBool() {
		return Request.super.predictBool();
	}
}
