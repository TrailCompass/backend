package space.itoncek.trailcompass.database;

import java.io.IOException;
import java.sql.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.objects.User;
import space.itoncek.trailcompass.objects.UserMeta;
import space.itoncek.trailcompass.objects.messages.Message;
import space.itoncek.trailcompass.objects.messages.MessageContent;

public class MariaDatabaseImpl implements DatabaseInterface {
	private static final Logger log = LoggerFactory.getLogger(MariaDatabaseImpl.class);
	private final Connection conn;

	public MariaDatabaseImpl(String url, String user, String password) throws SQLException {
		conn = DriverManager.getConnection(url, user, password);
	}

	@Override
	public void migrate() {
		log.info("This database is stored in MySQL!");
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
						`read` TINYINT(1) UNSIGNED NOT NULL,
						PRIMARY KEY (`id`) USING BTREE,
						INDEX `FK__users` (`sender`) USING BTREE,
						INDEX `FK__users_2` (`receiver`) USING BTREE,
						CONSTRAINT `FK__users` FOREIGN KEY (`sender`) REFERENCES `users` (`user_id`) ON UPDATE NO ACTION ON DELETE NO ACTION,
						CONSTRAINT `FK__users_2` FOREIGN KEY (`receiver`) REFERENCES `users` (`user_id`) ON UPDATE NO ACTION ON DELETE NO ACTION
					)
					COLLATE='utf8mb4_general_ci'
					ENGINE=InnoDB
					;""");

			stmt.executeUpdate("""
					CREATE TABLE IF NOT EXISTS `gamestate` (
						`key` VARCHAR(512) NOT NULL DEFAULT '0' COLLATE 'utf8mb4_general_ci',
						`value` MEDIUMTEXT NOT NULL COLLATE 'utf8mb4_general_ci',
						PRIMARY KEY (`key`) USING BTREE
					)
					COLLATE='utf8mb4_general_ci' ENGINE=InnoDB;""");

			stmt.executeUpdate("""
					CREATE TABLE IF NOT EXISTS `rest_periods` (
						`id` INT NOT NULL,
						`start` VARCHAR(255) NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
						`end` VARCHAR(255) NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
						PRIMARY KEY (`id`)
					)
					COLLATE='utf8mb4_general_ci' ENGINE=InnoDB;""");

			if (gamestateMissingKey(stmt, "game-start")) {
				stmt.executeUpdate("INSERT INTO gamestate VALUES ('game-start','1970-01-01T00:00:00Z[UTC]')");
			}

			if (gamestateMissingKey(stmt, "setup-lock")) {
				stmt.executeUpdate("INSERT INTO gamestate VALUES ('setup-lock','false')");
			}

			if (gamestateMissingKey(stmt, "hider")) {
				stmt.executeUpdate("INSERT INTO gamestate VALUES ('hider','1')");
			}

			if (gamestateMissingKey(stmt, "hiding-time")) {
				stmt.executeUpdate("INSERT INTO gamestate VALUES ('hiding-time','%s')".formatted(Duration.of(2, ChronoUnit.HOURS).getSeconds()));
			}

		} catch (SQLException e) {
			log.error("Unable to migrate!", e);
		}
	}

	private boolean gamestateMissingKey(Statement stmt, String key) throws SQLException {
		try (ResultSet rs = stmt.executeQuery("SELECT 1 FROM gamestate WHERE `key` = '%s';".formatted(key))) {
			return !rs.next();
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
	public int getCurrentHiderId() throws SQLException {
		String v = getValueFromKeystore("hider");
		if (v == null) return -1;
		else return Integer.parseInt(v);
	}

	@Override
	public boolean setCurrentHider(int i) throws SQLException {
		return setValueInKeystore("hider", String.valueOf(i));
	}

	@Nullable
	@Override
	public ZonedDateTime getStartTime() throws SQLException {
		try {
			String v = getValueFromKeystore("game-start");
			if(v == null) return null;
			else return ZonedDateTime.parse(v);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	@Nullable
	@Override
	public List<User> listUsers() throws SQLException {
		try(ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM users;")) {
			List<User> users = new ArrayList<>();
			while (rs.next()) {
				users.add(new User(rs.getInt("user_id"), rs.getString("user_nickname"),rs.getBoolean("user_isadmin"), rs.getBoolean("user_ishider")));
			}
			return users;
		}
	}

	@Override
	public boolean setSetupLocked(boolean lock) throws SQLException {
		return setValueInKeystore("setup-lock", Boolean.toString(lock));
	}

	@Override
	public boolean getSetupLocked() throws SQLException {
		String v = getValueFromKeystore("setup-lock");
		if (v == null) return false;
		else return Boolean.parseBoolean(v);
	}

	@Override
	public Duration setHidingTime() throws SQLException {
		String v = getValueFromKeystore("hiding-time");
		if (v == null) return null;
		else return Duration.ofSeconds(Long.parseLong(v));
	}

	@Override
	public boolean setHidingTime(Duration hiding_time) throws SQLException {
		return setValueInKeystore("hiding-time", String.valueOf(hiding_time.getSeconds()));
	}

	@Override
	public boolean isInRestPeriod(ZonedDateTime dateTime) throws SQLException {
		try(ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM rest_periods;")) {
			boolean result = false;
			while (rs.next()) {
				ZonedDateTime periodStart = ZonedDateTime.parse(rs.getString("start"));
				ZonedDateTime periodEnd = ZonedDateTime.parse(rs.getString("end"));
				result |= (dateTime.isAfter(periodStart) && dateTime.isBefore(periodEnd));
			}
			return result;
		}
	}

	private @Nullable String getValueFromKeystore(String key) throws SQLException {
		try	(PreparedStatement stmt = conn.prepareStatement("SELECT * FROM gamestate WHERE `key` = ? LIMIT 1;")) {
			stmt.setString(1,key);
			try(ResultSet rs = stmt.executeQuery()) {
				if(rs.next()) {
					return rs.getString(2);
				} else return null;
			}
		}
	}

	private boolean setValueInKeystore(String key, String value) throws SQLException {
		if(getValueFromKeystore(key) != null) {
			try (PreparedStatement stmt = conn.prepareStatement("UPDATE gamestate SET value=? WHERE `key`=?")) {
				stmt.setString(1,value);
				stmt.setString(2,key);
				return stmt.executeUpdate() == 1;
			}
		} else {
			try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO gamestate (`key`, value) VALUES (?,?)")) {
				stmt.setString(1,key);
				stmt.setString(2,value);
				return stmt.executeUpdate() == 1;
			}
		}
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
