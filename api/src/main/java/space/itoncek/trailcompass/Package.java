package space.itoncek.trailcompass;

import org.slf4j.Logger;

import java.util.List;

public interface Package {
	void onLoad(Logger logger);
	void onEnable();
	void onDisable();
	List<Card> getCards();
}
