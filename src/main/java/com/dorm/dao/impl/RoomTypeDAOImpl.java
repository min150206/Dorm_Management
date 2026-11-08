package com.dorm.dao.impl;

import com.dorm.dao.RoomTypeDAO;
import com.dorm.model.RoomType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Concrete DAO implementation for the 'room_type' table.
 */
public class RoomTypeDAOImpl extends AbstractDAO<RoomType, Integer> implements RoomTypeDAO {

    // ── AbstractDAO template methods ──────────────────────────────────

    @Override
    protected String getTableName() {
        return "room_type";
    }

    @Override
    protected String getIdColumnName() {
        return "room_type_id";
    }

    @Override
    protected RoomType mapRow(ResultSet rs) throws SQLException {
        return new RoomType(
                rs.getInt("room_type_id"),
                rs.getString("type_name"),
                rs.getInt("capacity"),
                rs.getLong("price")
        );
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO room_type (type_name, capacity, price) VALUES (?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE room_type SET type_name = ?, capacity = ?, price = ? WHERE room_type_id = ?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, RoomType entity) throws SQLException {
        ps.setString(1, entity.getTypeName());
        ps.setInt(2, entity.getCapacity());
        ps.setLong(3, entity.getPrice());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, RoomType entity) throws SQLException {
        ps.setString(1, entity.getTypeName());
        ps.setInt(2, entity.getCapacity());
        ps.setLong(3, entity.getPrice());
        ps.setInt(4, entity.getRoomTypeId());
    }

    // ── RoomTypeDAO-specific methods ──────────────────────────────────

    @Override
    public Optional<RoomType> findByTypeName(String typeName) throws SQLException {
        return querySingle("SELECT * FROM room_type WHERE type_name = ?", typeName);
    }
}
