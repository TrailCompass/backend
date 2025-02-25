package space.itoncek.trailcompass;

import space.itoncek.trailcompass.pkg.Card;
import space.itoncek.trailcompass.pkg.objects.Usage;

public class ExampleCard implements Card {
	@Override
	public String getName() {
		return "ExampleCard";
	}

	@Override
	public String getDescription() {
		return "This is a dummy implementation of a card!";
	}

	@Override
	public Usage canUse() {
		return new Usage(true,false,"dummy");
	}
}
