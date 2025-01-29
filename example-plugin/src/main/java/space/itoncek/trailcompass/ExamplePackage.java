package space.itoncek.trailcompass;

import org.slf4j.Logger;
import space.itoncek.trailcompass.objects.LocationSupplier;
import space.itoncek.trailcompass.radar.RadarCategory;

import java.util.List;

public class ExamplePackage implements Package{
	private Logger logger;
	private LocationSupplier locationSupplier;

	@Override
	public void onLoad(Logger logger, LocationSupplier locationSupplier) {
		this.logger = logger;
		this.locationSupplier = locationSupplier;
	}

	@Override
	public void onEnable() {
		logger.info("ExamplePackage has been loaded");
	}


	@Override
	public List<Card> getCards() {
		return List.of(new ExampleCard());
	}

	@Override
	public List<RequestCategory> getRequestCategories() {
		return List.of(new RadarCategory(locationSupplier,logger));
	}
	@Override
	public void onDisable() {
		logger.info("ExamplePackage has been unloaded");
	}

}