package space.itoncek.trailcompass.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.itoncek.trailcompass.objects.SimpleUser;
import space.itoncek.trailcompass.objects.User;

import java.io.IOException;
import java.sql.*;

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
						`class_name` VARCHAR(256) NOT NULL DEFAULT '' COLLATE 'utf8mb4_uca1400_ai_ci',
						`class_draw_cards` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0',
						`class_pick_card` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0',
						PRIMARY KEY (`class_id`) USING BTREE,
						UNIQUE INDEX `class_name` (`class_name`) USING BTREE
					)
					COLLATE='utf8mb4_uca1400_ai_ci'
					ENGINE=InnoDB
					;
					""");

			stmt.executeUpdate("""
					CREATE TABLE IF NOT EXISTS `request_types` (
						`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
						`request_class_id` INT(10) UNSIGNED NOT NULL,
						`name` VARCHAR(256) NOT NULL DEFAULT '' COLLATE 'utf8mb4_uca1400_ai_ci',
						`description` TEXT NOT NULL DEFAULT '' COLLATE 'utf8mb4_uca1400_ai_ci',
						`svg_icon_url` VARCHAR(4096) NOT NULL DEFAULT '' COLLATE 'utf8mb4_uca1400_ai_ci',
						PRIMARY KEY (`id`) USING BTREE,
						UNIQUE INDEX `name` (`name`) USING BTREE,
						UNIQUE INDEX `description` (`description`) USING HASH,
						INDEX `class_id` (`request_class_id`) USING BTREE,
						CONSTRAINT `class_id` FOREIGN KEY (`request_class_id`) REFERENCES `request_classes` (`class_id`) ON UPDATE CASCADE ON DELETE RESTRICT
					)
					COLLATE='utf8mb4_uca1400_ai_ci'
					ENGINE=InnoDB
					;
					""");

			stmt.executeUpdate("""
					CREATE TABLE IF NOT EXISTS `cards_curses` (
						`curse_id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
						`curse_name` VARCHAR(256) NOT NULL DEFAULT '0' COLLATE 'utf8mb4_uca1400_ai_ci',
						`curse_description` VARCHAR(8192) NOT NULL DEFAULT '0' COLLATE 'utf8mb4_uca1400_ai_ci',
						`curse_casting_cost` VARCHAR(1024) NOT NULL DEFAULT '0' COLLATE 'utf8mb4_uca1400_ai_ci',
						`curse_amount` INT(10) UNSIGNED NOT NULL,
						PRIMARY KEY (`curse_id`) USING BTREE,
						UNIQUE INDEX `card_name` (`curse_name`) USING BTREE,
						UNIQUE INDEX `card_description` (`curse_description`) USING HASH
					)
					COLLATE='utf8mb4_uca1400_ai_ci'
					ENGINE=InnoDB
					;
					""");

			stmt.executeUpdate("""
					CREATE TABLE IF NOT EXISTS `cards_powerups` (
						`powerup_id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
						`powerup_name` VARCHAR(256) NOT NULL DEFAULT '' COLLATE 'utf8mb4_uca1400_ai_ci',
						`powerup_icon` VARCHAR(8192) NOT NULL DEFAULT '' COLLATE 'utf8mb4_uca1400_ai_ci',
						`powerup_amount` INT(10) UNSIGNED NOT NULL,
						PRIMARY KEY (`powerup_id`) USING BTREE,
						UNIQUE INDEX `powerup_name` (`powerup_name`) USING BTREE
					)
					COLLATE='utf8mb4_uca1400_ai_ci'
					ENGINE=InnoDB
					;
					""");

			stmt.executeUpdate("""
					CREATE TABLE IF NOT EXISTS `cards_time_bonuses` (
						`time_bonus_id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
						`time_bonus_time` INT(10) UNSIGNED NOT NULL,
						`time_bonus_card_amount` INT(10) UNSIGNED NOT NULL,
						PRIMARY KEY (`time_bonus_id`) USING BTREE,
						UNIQUE INDEX `time_bonus_time` (`time_bonus_time`) USING BTREE
					)
					COLLATE='utf8mb4_uca1400_ai_ci'
					ENGINE=InnoDB
					;
					""");
		} catch (SQLException e) {
			log.error("Unable to migrate!",e);
		}
	}

	@Override
	public User getUserByID(int id) {
		return null;
	}

	@Override
	public SimpleUser getUserMeta(String username, String passwordhash) {
		return null;
	}

	@Override
	public boolean createUser(String name, String passwordhash) {
		return createUser(name,passwordhash,false);
	}

	@Override
	public boolean createUser(String name, String passwordhash, boolean isAdmin) {
		return false;
	}

	@Override
	public boolean needsDefaultUser() {
		try(Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery("SELECT TABLE_ROWS FROM information_schema.TABLES WHERE TABLE_NAME = 'users' AND TABLE_SCHEMA = 'tc_system';");
			return !rs.next() || rs.getInt("TABLE_ROWS") <= 0;
		} catch (SQLException e) {
			log.error("Unable to check default user presence!",e);
			return true;
		}
	}

	@Override
	public void close() throws IOException {
		try {
			conn.close();
		} catch (SQLException e) {
			throw new IOException("Unable to close MariaDB connection!",e);
		}
	}
}
