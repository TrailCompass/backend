package space.itoncek.trailcompass.modules;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.objects.Game;
import space.itoncek.trailcompass.objects.Permission;
import space.itoncek.trailcompass.objects.SimpleUser;
import space.itoncek.trailcompass.objects.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
					    db_name varchar(100) NOT NULL,
					    archived BOOL NOT NULL,
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

	@SuppressWarnings("SqlSourceToSinkFlow") // checked by RegEx!
	public boolean createGame(User user, String dbName) {
		try (Statement createDB = conn.createStatement();
		PreparedStatement createTableEntry = conn.prepareStatement("INSERT INTO tc_system.games (owner, db_name,archived) VALUES (?,?,false)")) {
			createTableEntry.setInt(1,user.id());
			createTableEntry.setString(2, dbName);

			if(dbName.matches("^[A-Za-z0-9]+$"))createDB.executeUpdate("CREATE DATABASE tc_%s;".formatted(dbName));
			else throw new SQLException("Attempted SQL injection! %s".formatted(dbName));
			createTableEntry.executeUpdate();
			return true;
		} catch (SQLException e) {
			log.error("Unable to create game tc_%s!".formatted(dbName), e);
			return false;
		}
	}

	public ArrayList<Game> listGames() {
		try(Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery("SELECT * FROM tc_system.games;");
			ArrayList<Game> games = new ArrayList<>();
			while (rs.next()) {
				Game g = new Game(rs.getInt("id"),rs.getInt("owner"),rs.getString("db_name"), rs.getBoolean("archived"));
				games.add(g);
			}
			return games;
		} catch (SQLException e) {
			log.error("Unable to list games!", e);
			return null;
		}
	}

	public Game getGame(int gameId) {
		try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM tc_system.games WHERE id=?;")) {
			stmt.setInt(1,gameId);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				return new Game(rs.getInt("id"),rs.getInt("owner"),rs.getString("db_name"), rs.getBoolean("archived"));
			} else return null;
		} catch (SQLException e) {
			log.error("Unable to get game #%d!".formatted(gameId), e);
			return null;
		}
	}

	public boolean archiveGame(Game game) {
		try (PreparedStatement stmt = conn.prepareStatement("UPDATE tc_system.games SET archived=true WHERE id=?;")) {
			stmt.setInt(1,game.id());
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			log.error("Unable to archive game #%d!".formatted(game.id()), e);
			return false;
		}
	}
	public boolean activateGame(Game game) {
		try (PreparedStatement stmt = conn.prepareStatement("UPDATE tc_system.games SET archived=false WHERE id=?;")) {
			stmt.setInt(1,game.id());
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			log.error("Unable to archive game #%d!".formatted(game.id()), e);
			return false;
		}
	}
}