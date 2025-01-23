package space.itoncek.trailcompass;

import org.slf4j.Logger;

import java.util.List;

public class ExamplePlugin implements Package{
	private Logger logger;

	@Override
	public void onLoad(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void onEnable() {
		logger.info("ExamplePlugin has been loaded");
	}

	@Override
	public void onDisable() {
		logger.info("ExamplePlugin has been unloaded");
	}

	@Override
	public List<Card> getCards() {
		return List.of(new ExampleCard());
	}

}