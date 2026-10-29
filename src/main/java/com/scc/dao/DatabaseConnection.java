package com.gateentry.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens SQLite connections and creates the tables on first run. */
public final class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:gate_entry.db";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    /** Creates the tables if they do not exist yet. */
    public static void initialize() {
        String users = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    username      TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    full_name     TEXT NOT NULL,
                    role          TEXT NOT NULL CHECK (role IN ('GUARD','ADMIN','RESIDENT')),
                    status        TEXT NOT NULL DEFAULT 'PENDING'
                                  CHECK (status IN ('PENDING','ACTIVE','REJECTED'))
                )""";

        String logs = """
                CREATE TABLE IF NOT EXISTS activity_logs (
                    log_id      INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id     INTEGER,
                    action      TEXT NOT NULL,
                    description TEXT,
                    timestamp   TEXT NOT NULL DEFAULT (datetime('now','localtime')),
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                )""";

        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute(users);
            st.execute(logs);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}