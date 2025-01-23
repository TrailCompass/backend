package space.itoncek.trailcompass;

/**
 * Represents a card, that can be played during a game.
 */
public interface Card {
	/**
	 * @return Name of the card
	 */
	String getName();

	/**
	 * @return Description of the card
	 */
	String getDescription();
}
