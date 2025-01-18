package space.itoncek.trailcompass.database;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.objects.*;

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
						`user_passwordhash` CHAR(64) NOT NULL COLLATE 'utf8mb4_general_ci',
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
					CREATE TABLE IF NOT EXISTS `request_classes` (
						`class_id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
						`class_name` VARCHAR(256) NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
						`class_draw_cards` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0',
						`class_pick_card` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0',
						PRIMARY KEY (`class_id`) USING BTREE,
						UNIQUE INDEX `class_name` (`class_name`) USING BTREE
					)
					COLLATE='utf8mb4_general_ci'
					ENGINE=InnoDB
					;
					""");

			stmt.executeUpdate("""
					CREATE TABLE IF NOT EXISTS `request_types` (
						`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
						`request_class_id` INT(10) UNSIGNED NOT NULL,
						`name` VARCHAR(256) NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
						`description` TEXT NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
						`svg_icon_url` VARCHAR(4096) NOT NULL DEFAULT '' COLLATE 'utf8mb4_general_ci',
						PRIMARY KEY (`id`) USING BTREE,
						UNIQUE INDEX `name` (`name`) USING BTREE,
						UNIQUE INDEX `description` (`description`) USING HASH,
						INDEX `class_id` (`request_class_id`) USING BTREE,
						CONSTRAINT `class_id` FOREIGN KEY (`request_class_id`) REFERENCES `request_classes` (`class_id`) ON UPDATE CASCADE ON DELETE RESTRICT
					)
					COLLATE='utf8mb4_general_ci'
					ENGINE=InnoDB
					;
					""");

			stmt.executeUpdate("""
					CREATE TABLE IF NOT EXISTS `card_definitions` (
						`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
						`type` VARCHAR(50) NOT NULL DEFAULT '' COLLATE 'utf8mb4_uca1400_ai_ci',
						`name` VARCHAR(256) NOT NULL COLLATE 'utf8mb4_uca1400_ai_ci',
						`amount_in_deck` INT(11) NOT NULL DEFAULT '1',
						`description` TEXT NULL DEFAULT NULL COMMENT 'mainly for curses and requests' COLLATE 'utf8mb4_uca1400_ai_ci',
						`casting_cost` TEXT NULL DEFAULT NULL COMMENT 'for curses' COLLATE 'utf8mb4_uca1400_ai_ci',
						`bonus_time` INT(11) NULL DEFAULT NULL COMMENT 'for time bonuses only',
						`icon` TEXT NULL DEFAULT NULL COMMENT 'for powerups and time bonuses' COLLATE 'utf8mb4_uca1400_ai_ci',
						PRIMARY KEY (`id`) USING BTREE,
						UNIQUE INDEX `type_name` (`type`, `name`) USING BTREE
					)
					COLLATE='utf8mb4_uca1400_ai_ci'
					ENGINE=InnoDB
					;
					""");
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
	public boolean addCurse(String title, String description, String casting_cost, int amount_in_deck) {
		try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO card_definitions (type,name,description,casting_cost,amount_in_deck) VALUES (?,?,?,?,?);")) {
			stmt.setString(1, CardType.CURSE.name());
			stmt.setString(2, title);
			stmt.setString(3, description);
			stmt.setString(4, casting_cost);
			stmt.setInt(5, amount_in_deck);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			log.error("Unable to save a card!", e);
			return false;
		}
	}

	@Override
	public boolean addPowerup(String title, String icon, int amount_in_deck) {
		try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO card_definitions (type,name,icon,amount_in_deck) VALUES (?,?,?,?);")) {
			stmt.setString(1, CardType.CURSE.name());
			stmt.setString(2, title);
			stmt.setString(3, icon);
			stmt.setInt(4, amount_in_deck);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			log.error("Unable to save a card!", e);
			return false;
		}
	}

	@Override
	public boolean addTimeBonus(String title, int bonus_time, int amount_in_deck) {
		try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO card_definitions (type,name,bonus_time,amount_in_deck) VALUES (?,?,?,?)")) {
			stmt.setString(1, CardType.CURSE.name());
			stmt.setString(2,title);
			stmt.setInt(3, bonus_time);
			stmt.setInt(4, amount_in_deck);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			log.error("Unable to save a card!", e);
			return false;
		}
	}

	@Override
	public @Nullable List<Card> listCards() {
		try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM card_definitions;");
			 ResultSet rs = stmt.executeQuery()) {
			ArrayList<Card> cards = new ArrayList<>();

			while (rs.next()) {
				Card card = switch (CardType.valueOf(rs.getString("type"))) {
					case CURSE ->
							new Curse(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getString("casting_cost"), rs.getInt("amount_in_deck"));
					case POWERUP ->
							new Powerup(rs.getInt("id"), rs.getString("name"), rs.getString("icon"), rs.getInt("amount_in_deck"));
					case TIME_BONUS ->
							new TimeBonus(rs.getInt("id"), rs.getString("name"), rs.getInt("bonus_time"), rs.getInt("amount_in_deck"));
					//required for null detection, we don't want any nulls here ;)
					//noinspection UnnecessaryDefault
					default -> null;
				};
				if (card != null) {
					cards.add(card);
				}
			}
			return cards;
		} catch (SQLException e) {
			log.error("Unable to save a curse!", e);
			return null;
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
