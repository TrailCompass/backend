package space.itoncek.trailcompass.modules;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.objects.Permission;
import space.itoncek.trailcompass.objects.SimpleUser;
import space.itoncek.trailcompass.objects.User;

import java.sql.*;
import java.util.ArrayList;

public class DBManager {
	private static final Logger log = LoggerFactory.getLogger(DBManager.class);
	private final Connection conn;
	private final String systemDB;

	public DBManager(String url, String user, String password, String systemDB) throws SQLException {
		this.systemDB = systemDB;
		conn = DriverManager.getConnection(url, user, password);
	}

	public void migrate() throws SQLException {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("""
					CREATE TABLE IF NOT EXISTS %s.keystore (
					    `key` varchar(100) NOT NULL,
					    value varchar(512) NOT NULL,
					    CONSTRAINT keystore_pk
					    PRIMARY KEY (`key`)
					)
					ENGINE=InnoDB
					DEFAULT CHARSET=utf8mb4;""".formatted(systemDB));
			stmt.executeUpdate("""
					CREATE TABLE IF NOT EXISTS %s.users (
					    id INT UNSIGNED auto_increment NOT NULL,
					    name varchar(100) NOT NULL,
					    passwordhash CHAR(128) NOT NULL,
					    permissions INT UNSIGNED NOT NULL,
					    CONSTRAINT PRIMARY KEY (id),
					    CONSTRAINT users_username_unique UNIQUE KEY (name),
					    CONSTRAINT users_password_unique UNIQUE KEY (passwordhash)
					)
					ENGINE=InnoDB
					DEFAULT CHARSET=utf8mb4;""".formatted(systemDB));
			stmt.executeUpdate("""
					CREATE TABLE IF NOT EXISTS %s.games (
					    id INT UNSIGNED auto_increment NOT NULL,
					    owner INT UNSIGNED NOT NULL,
					    db_name varchar(100) NOT NULL COMMENT 'without tc_',
					    CONSTRAINT games_pk PRIMARY KEY (id),
					    CONSTRAINT games_unique UNIQUE KEY (db_name),
					    CONSTRAINT games_users_FK FOREIGN KEY (owner) REFERENCES tc_system.users(id)
					)
					ENGINE=InnoDB
					DEFAULT CHARSET=utf8mb4;""".formatted(systemDB));
		}
	}

	public @Nullable String getKeystoreEntry(@NotNull String id) throws SQLException {
		try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tc_system.keystore WHERE `key`=?;")) {
			stmt.setString(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				String value = rs.getString("value");
				rs.close();
				stmt.close();
				return value;
			} else {
				return null;
			}
		}
	}

	public @NotNull String getKeystoreEntry(@NotNull String key, @NotNull String default_value) throws SQLException {
		String res = getKeystoreEntry(key);
		if (res != null) {
			return res;
		} else {
			setKeystoreEntry(key, default_value);
			return default_value;
		}
	}

	public void setKeystoreEntry(@NotNull String key, @NotNull String value) throws SQLException {
		try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM keystore WHERE `key`=?;")) {
			stmt.setString(1, key);
			if (stmt.executeQuery().next()) {
				try (PreparedStatement update = conn.prepareStatement("UPDATE `keystore` SET `value`=? WHERE `key`=?;")) {
					update.setString(1, value);
					update.setString(2, key);
					update.executeUpdate();
				}
			} else {
				try (PreparedStatement update = conn.prepareStatement("INSERT INTO `keystore` (`key`, `value`) VALUES (?, ?);")) {
					update.setString(1, key);
					update.setString(2, value);
					update.executeUpdate();
				}
			}
		}
	}

	public void close() throws SQLException {
		conn.close();
	}

	public @Nullable SimpleUser getSimpleUser(String name, String passwordhash) {
		try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tc_system.users WHERE name=? and passwordhash=?;")) {
			stmt.setString(1, name);
			stmt.setString(2, passwordhash);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return new SimpleUser(rs.getInt("id"), (int) (System.currentTimeMillis() + 600 * 1000));
			} else {
				log.info("Not found");
				return null;
			}
		} catch (SQLException e) {
			log.error("Unable to fetch this user: %s!".formatted(name), e);
			return null;
		}
	}

	public @Nullable User getUser(int requesterID) {
		try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tc_system.users WHERE id=? LIMIT 1;")) {
			stmt.setInt(1, requesterID);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return new User(rs.getInt("id"), rs.getString("name"), rs.getInt("permissions"));
			} else return null;
		} catch (SQLException e) {
			log.error("Unable to fetch this user: %d!".formatted(requesterID), e);
			return null;
		}
	}

	public boolean createUser(String name, String passwordhash, ArrayList<Permission> permissions) {
		try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO tc_system.users (name, passwordhash, permissions) VALUES (?,?,?);")) {
			stmt.setString(1, name);
			stmt.setString(2, passwordhash);
			stmt.setInt(3, Permission.convertToInt(permissions));
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			log.error("Unable to create user %s!".formatted(name), e);
			return false;
		}
	}
}