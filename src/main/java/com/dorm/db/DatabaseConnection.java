package com.dorm.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
 * Singleton PostgreSQL connection manager.
 * Note: A connection pool (e.g., HikariCP) is recommended for concurrent environments.
 */
public class DatabaseConnection {

    // Database credentials and connection URL
    private static final String URL  = "jdbc:postgresql://localhost:5432/dorm_management";
    private static final String USER = "postgres";
    private static final String PASS = "postgres";

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

    // Auto-reconnect if connection was closed or dropped
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
