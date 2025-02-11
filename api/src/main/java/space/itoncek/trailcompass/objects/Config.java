package space.itoncek.trailcompass.objects;

import com.geodesk.feature.FeatureLibrary;
import org.slf4j.Logger;

import java.io.File;

public record Config(Logger logger, LocationSupplier locationSupplier, File dataFolder, FeatureLibrary featureLibrary) {
}
