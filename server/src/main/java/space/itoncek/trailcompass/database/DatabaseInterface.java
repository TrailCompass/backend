package space.itoncek.trailcompass.database;

import org.jetbrains.annotations.Nullable;
import space.itoncek.trailcompass.objects.messages.Message;
import space.itoncek.trailcompass.objects.messages.MessageContent;
import space.itoncek.trailcompass.objects.GameState;
import space.itoncek.trailcompass.objects.User;
import space.itoncek.trailcompass.objects.UserMeta;

import java.io.Closeable;
import java.sql.SQLException;
import java.time.ZonedDateTime;
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

	/**
	 * @return true if database is healthy
	 *
	 * @throws SQLException database access error
	 */
	boolean isHealthy() throws SQLException;

	/**
	 * @param senderId ID of the player, that is sending this message
	 * @param receiverId ID of the player, that is receiving this message
	 * @param content content of the message
	 *
	 * @return true if the insert went allright
	 *
	 * @throws SQLException database access error
	 */
	boolean createMessage(int senderId, int receiverId, MessageContent content) throws SQLException;

	/**
	 * @param receiverId ID of the player, that is receiving these messages
	 *
	 * @return List of messages, might be null
	 *
	 * @throws SQLException database access error
	 */
	@Nullable List<Message> getMessages(int receiverId) throws SQLException;

	/**
	 * @return current hider ID
	 *
	 * @throws SQLException database access error
	 */
    int getCurrentHiderId() throws SQLException;

	/**
	 * @param i ID of a player
	 *
	 * @return true if the insert went alright
	 *
	 * @throws SQLException database access error
	 */
	boolean setCurrentHider(int i) throws SQLException;

	/**
	 * @return {@link ZonedDateTime} of the start of the game
	 *
	 * @throws SQLException database access error
	 */
	@Nullable ZonedDateTime getStartTime() throws SQLException;

	/**
	 * @return game state enum
	 *
	 * @throws SQLException database access error
	 */
	@Nullable GameState getGameState() throws SQLException;

	/**
	 * @return List all players
	 *
	 * @throws SQLException database access error
	 */
	@Nullable List<User> listUsers() throws SQLException;
}
