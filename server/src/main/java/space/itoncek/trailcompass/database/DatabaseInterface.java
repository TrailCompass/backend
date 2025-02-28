package space.itoncek.trailcompass.database;

import org.jetbrains.annotations.Nullable;
import space.itoncek.trailcompass.messages.Message;
import space.itoncek.trailcompass.messages.MessageContent;
import space.itoncek.trailcompass.pkg.objects.UserMeta;
import space.itoncek.trailcompass.pkg.objects.User;

import java.io.Closeable;
import java.sql.SQLException;
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

	boolean isHealthy() throws SQLException;

	boolean createMessage(int senderId, int receiverId, MessageContent content) throws SQLException;

	List<Message> getMessages(int receiverId) throws SQLException;

    int getCurrentHiderId();

	boolean setCurrentHider(int i);
}
