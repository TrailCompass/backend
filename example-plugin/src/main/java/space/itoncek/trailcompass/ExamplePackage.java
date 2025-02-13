package space.itoncek.trailcompass;

import com.geodesk.feature.FeatureLibrary;
import org.slf4j.Logger;
import space.itoncek.trailcompass.measuring.MeasuringCategory;
import space.itoncek.trailcompass.objects.Config;
import space.itoncek.trailcompass.objects.LocationSupplier;
import space.itoncek.trailcompass.radar.RadarCategory;

import java.io.File;
import java.util.List;

public class ExamplePackage implements Package{
	private Config c;
	private FeatureLibrary flib;

	@Override
	public void onLoad(Config cfg) {
		this.c = cfg;
	}

	@Override
	public void onEnable() {
		c.logger().info("Loading Geodesk database");
		flib = c.featureLibrary();
	}

	@Override
	public List<Card> getCards() {
		return List.of(new ExampleCard());
	}

	@Override
	public List<RequestCategory> getRequestCategories() {
		return List.of(
				new RadarCategory(c.locationSupplier(),c.logger()),
				new MeasuringCategory(c.featureLibrary(), c.locationSupplier(), c.logger()));
	}

	@Override
	public void onDisable() {
		flib.close();
		c.logger().info("ExamplePackage has been unloaded");
	}

}