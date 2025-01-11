/*
 * ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░
 * ░        ░░░      ░░░       ░░░░      ░░░  ░░░░░░░░  ░░░░░░░░░      ░░
 * ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒
 * ▓▓▓▓  ▓▓▓▓▓  ▓▓ ▓  ▓▓       ▓▓▓  ▓▓▓▓  ▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓▓      ▓▓
 * ████  █████  ███   ██  ████████  ████  ██  ████████  ██████████████  █
 * █        ███      █ █  █████████      ███        ██        ███      ██
 * ██████████████████████████████████████████████████████████████████████
 *
 *               Created by IToncek for iQLandia
 */

package space.itoncek.trailcompass.modules;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.itoncek.trailcompass.objects.SimpleUser;

import java.sql.*;

public class DBManager {
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
                        passwordhash CHAR(64) NOT NULL,
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
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM keystore WHERE `id`='%s';".formatted(id));
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
        try (Statement stmt = conn.createStatement()) {
            if (stmt.executeQuery("SELECT * FROM keystore WHERE `id`='%s';".formatted(key)).next()) {
                stmt.executeUpdate("UPDATE `keystore` SET `value`='%s' WHERE `id`='%s';".formatted(value, key));
            } else {
                stmt.executeUpdate("INSERT INTO `keystore` (`id`, `value`) VALUES ('%s', '%s');".formatted(key, value));
            }
        }
    }

    public void close() throws SQLException {
        conn.close();
    }

    public @Nullable SimpleUser getUser(String name, String passwordhash) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM %s.users WHERE name='%s' and passwordhash='%s';".formatted(systemDB, name, passwordhash));
            if (rs.next()) {
                return new SimpleUser(rs.getInt("id"), (int) (System.currentTimeMillis() + 600 * 1000));
            } else return null;
        }
    }
}