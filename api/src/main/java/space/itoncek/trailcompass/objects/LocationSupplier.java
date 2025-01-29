package space.itoncek.trailcompass.objects;

import java.util.concurrent.Callable;

public record LocationSupplier(Callable<Location> getSeekerLocation, Callable<Location> getHiderLocation) {
}
