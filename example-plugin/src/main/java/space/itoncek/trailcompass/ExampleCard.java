package space.itoncek.trailcompass;

public class ExampleCard implements Card {
	@Override
	public String getName() {
		return "ExampleCard";
	}

	@Override
	public String getDescription() {
		return "This is a dummy implementation of a card!";
	}
}
