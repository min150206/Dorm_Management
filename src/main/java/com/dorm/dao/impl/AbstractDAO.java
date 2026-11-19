package com.dorm.dao.impl;

import com.dorm.dao.GenericDAO;
import com.dorm.db.DatabaseConnection;
import com.dorm.model.BaseEntity;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * Base JDBC DAO providing common CRUD implementations.
 * Subclasses provide table specifics and parameter mappings.
 */
public abstract class AbstractDAO<T extends BaseEntity<ID>, ID extends Serializable>
        implements GenericDAO<T, ID> {

    protected Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Template methods for entity-specific mappings and SQL statements
    protected abstract T mapRow(ResultSet rs) throws SQLException;
    protected abstract String getTableName();
    protected abstract String getIdColumnName();
    protected abstract String getInsertSQL();
    protected abstract String getUpdateSQL();
    protected abstract void setInsertParameters(PreparedStatement ps, T entity) throws SQLException;
    protected abstract void setUpdateParameters(PreparedStatement ps, T entity) throws SQLException;

    @Override
    public T save(T entity) throws SQLException {
        String sql = getInsertSQL();
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setInsertParameters(ps, entity);
            ps.executeUpdate();

            // Retrieve auto-generated ID for SERIAL columns
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

    // Helper to execute a query with parameters and map all rows
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

    // Helper to query a single record (returns empty if not found)
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
