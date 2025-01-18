package space.itoncek.trailcompass.database;

import org.jetbrains.annotations.Nullable;
import space.itoncek.trailcompass.objects.Card;
import space.itoncek.trailcompass.objects.UserMeta;
import space.itoncek.trailcompass.objects.User;

import java.io.Closeable;
import java.util.List;

public interface DatabaseInterface extends Closeable {
	/**
	 * Prepares database for usage. Executed on every start, needs to not fail, if database is already ready!
	 */
	void migrate();

	/**
	 * Fetches full user meta from the database.
	 *
	 * @param id database-generated ID of the specified user
	 *
	 * @return {@link User} object, can be null
	 */
	@Nullable User getUserByID(int id);

	/**
	 * Fetches simple user meta.
	 *
	 * @param username     username of the requested user
	 * @param passwordhash SHA256-hash of the user's password
	 *
	 * @return {@link UserMeta} object, can be null
	 */
	@Nullable UserMeta getUserMeta(String username, String passwordhash);

	/**
	 * Creates a new user in the database. The new user is never marked as an admin.
	 *
	 * @param name         username of the new user
	 * @param passwordhash SHA256-hash of the new user's password
	 *
	 * @return {@code true} if operation completed successfully, {@code false} otherwise
	 */
	boolean createUser(String name, String passwordhash);

	/**
	 * Creates a new user in the database.
	 *
	 * @param name         username of the new user
	 * @param passwordhash SHA256-hash of the new user's password
	 * @param isAdmin      should the new user be marked as admin?
	 *
	 * @return {@code true} if operation completed successfully, {@code false} otherwise
	 */
	boolean createUser(String name, String passwordhash, boolean isAdmin);

	/**
	 * @return {@code true} if there is no user registered, {@code false} otherwise
	 */
	boolean needsDefaultUser();

	boolean addCurse(String title, String description, String casting_cost, int amount_in_deck);
	boolean addPowerup(String name, String icon, int amount_in_deck);
	boolean addTimeBonus(String title, int bonus_time, int amount_in_deck);

	@Nullable List<Card> listCards();
}
