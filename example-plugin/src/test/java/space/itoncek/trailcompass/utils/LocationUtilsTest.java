package space.itoncek.trailcompass.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import space.itoncek.trailcompass.pkg.objects.Location;

import java.util.HashMap;
import java.util.Map;

class LocationUtilsTest {

	@Test
	void calculateDistance() {
		HashMap<Map.Entry<Location, Location>,Double> entries = new HashMap<>();
		entries.put(Map.entry(
				new Location(50.0544700, 14.2905664,0),
				new Location(50.1089592, 14.5773658,0)
		),21342.14/1000);
		entries.put(Map.entry(
				new Location(0, 0,0),
				new Location(0, 0,0)
		),0.);
		entries.put(Map.entry(
				new Location(50.0865062, 14.4206464,0),
				new Location(50.0865429, 14.4192425,0)
		),100.25/1000);
		entries.put(Map.entry(
				new Location(50.0903099, 14.3981389,0),
				new Location(50.0813244, 14.3976894,0)
		),999.66/1000);
		entries.put(Map.entry(
				new Location(50.2523749, 12.0906510,0),
				new Location(49.5505908, 18.8592416,0)
		),490835.71/1000);
		entries.put(Map.entry(
				new Location(48.5518262, 14.3332266,0),
				new Location(51.0557004, 14.3155804,0)
		),278420.99/1000);

		for (Map.Entry<Map.Entry<Location, Location>, Double> entry : entries.entrySet()) {
			Map.Entry<Location, Location> locpair = entry.getKey();
			Double result = entry.getValue();

			double v = LocationUtils.calculateDistance(locpair.getKey(), locpair.getValue());
			double diff = Math.abs(v - result);
			System.out.println("Diff = " + diff*1000 + "m");
			assertTrue(diff < 1d / 1000);
		}
	}
}