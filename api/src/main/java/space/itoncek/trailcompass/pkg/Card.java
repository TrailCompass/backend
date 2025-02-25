package space.itoncek.trailcompass.pkg;

import space.itoncek.trailcompass.pkg.objects.Usage;

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

	/**
	 * @return {@link Usage} object, representing the card usability result
	 */
	Usage canUse();
}
