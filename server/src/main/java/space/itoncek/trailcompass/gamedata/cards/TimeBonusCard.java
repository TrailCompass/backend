package space.itoncek.trailcompass.gamedata.cards;

import space.itoncek.trailcompass.objects.Card;

public class TimeBonusCard implements Card {
	private final int minutes;
	private final int amountInDeck;

	public TimeBonusCard(int minutes, int amountInDeck) {
		this.minutes = minutes;
		this.amountInDeck = amountInDeck;
	}
	@Override
	public String getID() {
		return "space.itoncek.trailcompass.gamedata.TimeBonusCard."+minutes+"m";
	}

	@Override
	public String getName() {
		return minutes+" minute bonus";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public boolean canUse() {
		return false;
	}

	@Override
	public int amountInDeck() {
		return amountInDeck;
	}
}
