package com.dorm.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton database connection manager for PostgreSQL.
 * The desktop application shares one connection; service transactions synchronize on this manager.
 *
 * Configure DORM_DB_URL, DORM_DB_USER and DORM_DB_PASSWORD before first use.
 */
public class DatabaseConnection {

    // ── Connection configuration ──────────────────────────────────────
    private static String setting(String name, String fallback) {
        return System.getProperty(name, System.getenv().getOrDefault(name, fallback));
    }
    private static final String URL  = setting("DORM_DB_URL", "jdbc:postgresql://localhost:5432/dorm_management");
    private static final String USER = setting("DORM_DB_USER", "postgres");
    private static final String PASS = setting("DORM_DB_PASSWORD", "postgres");

    private static volatile DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("[DB] Connected to PostgreSQL successfully.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC driver not found.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the singleton instance, creating it if necessary (double-checked locking).
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Returns the active JDBC connection.
     * Automatically reconnects if the connection was closed.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("[DB] Reconnected to PostgreSQL.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reconnect to database: " + e.getMessage(), e);
        }
        return connection;
    }

    /** Closes the connection and resets the singleton. */
    public static void closeIfOpen() {
        if (instance != null) instance.closeConnection();
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        } finally {
            instance = null;
        }
    }
}
