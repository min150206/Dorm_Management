package com.dorm.dao.impl;

import com.dorm.dao.GenericDAO;
import com.dorm.db.DatabaseConnection;
import com.dorm.model.BaseEntity;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract DAO implementation that provides reusable CRUD logic via JDBC.
 *
 * Concrete subclasses only need to implement:
 *   - {@link #mapRow(ResultSet)}         — convert a ResultSet row → entity
 *   - {@link #setInsertParameters(PreparedStatement, BaseEntity)}  — bind INSERT params
 *   - {@link #setUpdateParameters(PreparedStatement, BaseEntity)}  — bind UPDATE params
 *   - {@link #getInsertSQL()}            — the INSERT statement
 *   - {@link #getUpdateSQL()}            — the UPDATE statement
 *
 * @param <T>  entity type
 * @param <ID> primary-key type
 */
public abstract class AbstractDAO<T extends BaseEntity<ID>, ID extends Serializable>
        implements GenericDAO<T, ID> {

    // ── Connection helper ─────────────────────────────────────────────

    /** Shortcut to obtain the shared JDBC connection. */
    protected Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── Abstract template methods (subclasses must implement) ─────────

    /** Map the current ResultSet row to an entity instance. */
    protected abstract T mapRow(ResultSet rs) throws SQLException;

    /** Return the table name for this entity (e.g. "account"). */
    protected abstract String getTableName();

    /** Return the primary-key column name (e.g. "account_id"). */
    protected abstract String getIdColumnName();

    /** Return the full INSERT SQL (with ? placeholders). */
    protected abstract String getInsertSQL();

    /** Return the full UPDATE SQL (with ? placeholders). */
    protected abstract String getUpdateSQL();

    /** Bind values into a PreparedStatement for INSERT. */
    protected abstract void setInsertParameters(PreparedStatement ps, T entity) throws SQLException;

    /** Bind values into a PreparedStatement for UPDATE. */
    protected abstract void setUpdateParameters(PreparedStatement ps, T entity) throws SQLException;

    // ── GenericDAO implementation ─────────────────────────────────────

    @Override
    public T save(T entity) throws SQLException {
        String sql = getInsertSQL();
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setInsertParameters(ps, entity);
            ps.executeUpdate();

            // For SERIAL PKs, retrieve the generated key
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    @SuppressWarnings("unchecked")
                    ID generatedId = (ID) keys.getObject(1);
                    entity.setId(generatedId);
                }
            }
        }
        return entity;
    }

    @Override
    public T update(T entity) throws SQLException {
        String sql = getUpdateSQL();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            setUpdateParameters(ps, entity);
            ps.executeUpdate();
        }
        return entity;
    }

    @Override
    public boolean deleteById(ID id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<T> findById(ID id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<T> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName();
        List<T> results = new ArrayList<>();
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        }
        return results;
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    @Override
    public boolean existsById(ID id) throws SQLException {
        String sql = "SELECT 1 FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── Utility helpers for subclasses ────────────────────────────────

    /** Execute a parameterised query and map every row. */
    protected List<T> queryList(String sql, Object... params) throws SQLException {
        List<T> results = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    /** Execute a parameterised query and return the first row (or empty). */
    protected Optional<T> querySingle(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }
}
