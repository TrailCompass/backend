package space.itoncek.trailcompass.gamedata.utils;

import space.itoncek.trailcompass.commons.objects.Location;

public class LocationUtils {
	public static double calculateDistance(Location l1, Location l2) {
		long earthRadiusKm = 6371;

		double dLat = Math.toRadians(l2.lat()-l1.lat());
		double dLon = Math.toRadians(l2.lon()-l1.lon());

		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
				Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(Math.toRadians(l1.lat())) * Math.cos(Math.toRadians(l2.lat()));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return earthRadiusKm * c;
	}
}
