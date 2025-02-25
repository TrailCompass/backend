package space.itoncek.trailcompass.pkg.objects;

import java.util.concurrent.Callable;

/**
 * Provides a bridge between database and packages, carries accurate location information
 * @param getSeekerLocation callback, returns seekers' average location
 * @param getHiderLocation callback, returns hider's location
 */
public record LocationSupplier(Callable<Location> getSeekerLocation, Callable<Location> getHiderLocation) {
}
