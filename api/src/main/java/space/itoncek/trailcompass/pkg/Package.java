package space.itoncek.trailcompass.pkg;

import space.itoncek.trailcompass.pkg.objects.Config;

import java.util.List;

/**
 * Main class, representing a {@link Package}, that is to be loaded by the server.
 */
public interface Package {
	/**
	 * This method acts as the constructor, gets called with necessary objects for optimal experience.
	 * <p><b>NO LOGIC SHOULD BE DONE IN HERE! THE SERVER IS PROBABLY NOT READY!</b>
	 * <p>This method should only save the input parameters into variables and return without doing any logic.
	 * @param cfg Object supplying information access to the package
	 */
	void onLoad(Config cfg);

	/**
	 * Gets called on server startup. Server is ready to interact with clients, preliminary setup shall be done in this method.
	 */
	void onEnable();

	/**
	 * This method defines the cards, this plugin supplies
	 * @hidden  Needs to be decided, whether this method should be called only once or any time the server is requesting cards.
	 * @return List of cards to be registered in the game.
	 */
	List<Card> getCards();

	/**
	 * This method defines the request classes, this plugin supplies
	 * @hidden  Needs to be decided, whether this method should be called only once or any time the server is requesting cards.
	 * @return List of request classes to be registered in the game.
	 */
	List<RequestCategory> getRequestCategories();

	/**
	 * Gets called on server shutdown. Any open connection shall be closed here. After this method returns, this package
	 * is considered dead.
	 */
	void onDisable();
}
