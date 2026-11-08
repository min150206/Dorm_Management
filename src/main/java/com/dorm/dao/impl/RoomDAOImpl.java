package com.dorm.dao.impl;

import com.dorm.dao.RoomDAO;
import com.dorm.model.Room;
import com.dorm.model.enums.RoomStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Concrete DAO implementation for the 'room' table.
 */
public class RoomDAOImpl extends AbstractDAO<Room, Integer> implements RoomDAO {

    // ── AbstractDAO template methods ──────────────────────────────────

    @Override
    protected String getTableName() {
        return "room";
    }

    @Override
    protected String getIdColumnName() {
        return "room_id";
    }

    @Override
    protected Room mapRow(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("room_id"),
                rs.getInt("room_type_id"),
                rs.getString("room_number"),
                rs.getString("building"),
                rs.getInt("current_occupancy"),
                RoomStatus.fromString(rs.getString("room_status"))
        );
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO room (room_type_id, room_number, building, current_occupancy, room_status) "
             + "VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE room SET room_type_id = ?, room_number = ?, building = ?, "
             + "current_occupancy = ?, room_status = ? WHERE room_id = ?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Room entity) throws SQLException {
        ps.setInt(1, entity.getRoomTypeId());
        ps.setString(2, entity.getRoomNumber());
        ps.setString(3, entity.getBuilding());
        ps.setInt(4, entity.getCurrentOccupancy());
        ps.setString(5, entity.getRoomStatus().name());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Room entity) throws SQLException {
        ps.setInt(1, entity.getRoomTypeId());
        ps.setString(2, entity.getRoomNumber());
        ps.setString(3, entity.getBuilding());
        ps.setInt(4, entity.getCurrentOccupancy());
        ps.setString(5, entity.getRoomStatus().name());
        ps.setInt(6, entity.getRoomId());
    }

    // ── RoomDAO-specific methods ──────────────────────────────────────

    @Override
    public List<Room> findByBuilding(String building) throws SQLException {
        return queryList("SELECT * FROM room WHERE building = ?", building);
    }

    @Override
    public List<Room> findByStatus(RoomStatus status) throws SQLException {
        return queryList("SELECT * FROM room WHERE room_status = ?", status.name());
    }

    @Override
    public List<Room> findByRoomTypeId(int roomTypeId) throws SQLException {
        return queryList("SELECT * FROM room WHERE room_type_id = ?", roomTypeId);
    }

    @Override
    public List<Room> findAvailableRooms() throws SQLException {
        return queryList(
            "SELECT r.* FROM room r "
          + "JOIN room_type rt ON r.room_type_id = rt.room_type_id "
          + "WHERE r.room_status = 'AVAILABLE' AND r.current_occupancy < rt.capacity"
        );
    }

    @Override
    public void updateOccupancy(int roomId, int newOccupancy) throws SQLException {
        String sql = "UPDATE room SET current_occupancy = ? WHERE room_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, newOccupancy);
            ps.setInt(2, roomId);
            ps.executeUpdate();
        }
    }
}
