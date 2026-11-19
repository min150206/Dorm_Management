package com.dorm.db;

import com.dorm.exception.ServiceException;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    @FunctionalInterface
    public interface TransactionWork<T> {
        T execute() throws SQLException;
    }

    public static <T> T execute(TransactionWork<T> work) {
        Connection c = DatabaseConnection.getInstance().getConnection();
        boolean oldAutoCommit;
        try {
            oldAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
        } catch (SQLException e) {
            throw new ServiceException("Failed to begin database transaction: " + e.getMessage(), e);
        }

        try {
            T result = work.execute();
            c.commit();
            return result;
        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException rollbackEx) {
                // Suppress rollback error
            }
            throw new ServiceException("Database operation failed: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            try {
                c.rollback();
            } catch (SQLException rollbackEx) {
                // Suppress rollback error
            }
            throw e;
        } finally {
            try {
                c.setAutoCommit(oldAutoCommit);
            } catch (SQLException e) {
                // Suppress reset error
            }
        }
    }
}
