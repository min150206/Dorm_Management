package com.dorm.logic.code;

import com.dorm.dao.impl.StudentDAOImpl;
import com.dorm.db.DatabaseConnection;
import com.dorm.model.enums.Role;
import com.dorm.logic.code.Session;
import java.sql.*;

class ServiceSupport {
    static String text(String value, String field, int max) {
        if (value == null || value.trim().isEmpty() || value.trim().length() > max)
            throw new IllegalArgumentException(field + " is required (max " + max + " characters).");
        return value.trim();
    }
    static void phone(String value) {
        if (value == null || !value.matches("0[0-9]{9}"))
            throw new IllegalArgumentException("Phone must have 10 digits starting with 0.");
    }
    static void ownStudent(String id) throws SQLException {
        Session.requireLogin();
        if (Session.getAccount().getRole() == Role.STUDENT) {
            String ownId = new StudentDAOImpl().findByAccountId(Session.getAccount().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Student profile is missing.")).getId();
            if (!ownId.equals(id)) throw new IllegalArgumentException("You can only access your own records.");
        }
    }
    interface Work<T> { T run() throws SQLException; }
    static <T> T transaction(Work<T> work) throws SQLException {
        // All DAOs use this connection. Keep a complete operation inside the same lock.
        synchronized (DatabaseConnection.getInstance()) {
            Connection c = DatabaseConnection.getInstance().getConnection();
            boolean old = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                T result = work.run();
                c.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally { c.setAutoCommit(old); }
        }
    }
    static void lock(String sql, Object id) throws SQLException {
        try (PreparedStatement p = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            p.setObject(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("Record does not exist.");
            }
        }
    }
}
