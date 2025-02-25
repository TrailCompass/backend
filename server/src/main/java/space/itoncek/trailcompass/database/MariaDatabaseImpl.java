package space.itoncek.trailcompass.database;

import org.mariadb.jdbc.export.Prepare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.messages.Message;
import space.itoncek.trailcompass.messages.MessageContent;
import space.itoncek.trailcompass.pkg.objects.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MariaDatabaseImpl implements DatabaseInterface {
	private static final Logger log = LoggerFactory.getLogger(MariaDatabaseImpl.class);
	private final Connection conn;

	public MariaDatabaseImpl(String url, String user, String password) throws SQLException {
		conn = DriverManager.getConnection(url, user, password);
	}

	@Override
	public void migrate() {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("""
					CREATE TABLE IF NOT EXISTS `users` (
						`user_id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
						`user_nickname` VARCHAR(100) NOT NULL COLLATE 'utf8mb4_general_ci',
						`user_passwordhash` CHAR(128) NOT NULL COLLATE 'utf8mb4_general_ci',
						`user_isadmin` TINYINT(1) UNSIGNED NOT NULL DEFAULT '0',
						`user_ishider` TINYINT(1) UNSIGNED NOT NULL DEFAULT '0',
						PRIMARY KEY (`user_id`) USING BTREE,
						UNIQUE INDEX `users_unique` (`user_nickname`) USING BTREE
					)
					COLLATE='utf8mb4_general_ci'
					ENGINE=InnoDB
					;
					""");

			stmt.executeUpdate("""
					CREATE TABLE IF NOT EXISTS `messages` (
						`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
						`sender` INT(10) UNSIGNED NOT NULL DEFAULT '0',
						`receiver` INT(10) UNSIGNED NOT NULL DEFAULT '0',
						`content` MEDIUMTEXT NOT NULL COLLATE 'utf8mb4_general_ci',
						`read` TINYINT(3) UNSIGNED NOT NULL,
						PRIMARY KEY (`id`) USING BTREE,
						INDEX `FK__users` (`sender`) USING BTREE,
						INDEX `FK__users_2` (`receiver`) USING BTREE,
						CONSTRAINT `FK__users` FOREIGN KEY (`sender`) REFERENCES `users` (`user_id`) ON UPDATE NO ACTION ON DELETE NO ACTION,
						CONSTRAINT `FK__users_2` FOREIGN KEY (`receiver`) REFERENCES `users` (`user_id`) ON UPDATE NO ACTION ON DELETE NO ACTION
					)
					COLLATE='utf8mb4_general_ci'
					ENGINE=InnoDB
					;""");

		} catch (SQLException e) {
			log.error("Unable to migrate!", e);
		}
	}

	@Override
	public User getUserByID(int id) {
		try(PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE user_id=?;")) {
			stmt.setInt(1,id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				return new User(rs.getInt("user_id"), rs.getString("user_nickname"),rs.getBoolean("user_isadmin"), rs.getBoolean("user_ishider"));
			} else {
				return null;
			}
		} catch (SQLException e) {
			log.error("Unable to get user from database! ",e);
			return null;
		}
	}

	@Override
	public UserMeta getUserMeta(String username, String passwordhash) {
		try(PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE user_nickname=? AND user_passwordhash=?;")) {
			stmt.setString(1,username);
			stmt.setString(2,passwordhash);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				return new UserMeta(rs.getInt("user_id"), System.currentTimeMillis() + 600000);
			} else {
				return null;
			}
		} catch (SQLException e) {
			log.error("Unable to get user meta from database! ",e);
			return null;
		}
	}

	@Override
	public boolean createUser(String name, String passwordhash) {
		return createUser(name, passwordhash, false);
	}

	@Override
	public boolean createUser(String nickname, String passwordhash, boolean isAdmin) {
		try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (user_nickname, user_passwordhash, user_isadmin) VALUES (?,?,?);")) {
			stmt.setString(1,nickname);
			stmt.setString(2, passwordhash);
			stmt.setBoolean(3, isAdmin);
			return stmt.executeUpdate()>0;
		} catch (SQLException e) {
			log.error("Unable to save user into the database!");
			return false;
		}
	}

	@Override
	public boolean needsDefaultUser() {
		try (Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery("SELECT TABLE_ROWS FROM information_schema.TABLES WHERE TABLE_NAME = 'users' AND TABLE_SCHEMA = 'tc_system';");
			return !rs.next() || rs.getInt("TABLE_ROWS") <= 0;
		} catch (SQLException e) {
			log.error("Unable to check default user presence!", e);
			return true;
		}
	}

	@Override
	public boolean isHealthy() throws SQLException {
		return conn.isValid(2);
	}

	@Override
	public boolean createMessage(int senderId, int receiverId, MessageContent content) throws SQLException {
		try(PreparedStatement stmt = conn.prepareStatement("INSERT INTO messages(sender, receiver, content,`read`) VALUES (%d,%d, %s,0);")) {
			stmt.setInt(1,senderId);
			stmt.setInt(2,receiverId);
			stmt.setString(3,content.serialize());
			return stmt.executeUpdate()>0;
		}
	}

	@Override
	public List<Message> getMessages(int receiverId) throws SQLException {
		List<Message> msgs = new ArrayList<>();
		try	(PreparedStatement stmt = conn.prepareStatement("SELECT * FROM messages WHERE receiver = ?;")) {
			stmt.setInt(1,receiverId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Message msg = new Message(rs.getInt("id"), rs.getInt("sender"), MessageContent.decode(rs.getString("content")),rs.getBoolean("read"));
				msgs.add(msg);
			}
		}
		return msgs;
	}

	@Override
	public void close() throws IOException {
		try {
			conn.close();
		} catch (SQLException e) {
			throw new IOException("Unable to close MariaDB connection!", e);
		}
	}
}
