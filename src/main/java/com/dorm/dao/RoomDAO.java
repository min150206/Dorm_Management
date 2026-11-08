package com.dorm.dao;

import com.dorm.model.Room;
import com.dorm.model.enums.RoomStatus;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for the 'room' table.
 */
public interface RoomDAO extends GenericDAO<Room, Integer> {

    /** Find all rooms in a specific building. */
    List<Room> findByBuilding(String building) throws SQLException;

    /** Find all rooms with a given status. */
    List<Room> findByStatus(RoomStatus status) throws SQLException;

    /** Find all rooms of a given room type. */
    List<Room> findByRoomTypeId(int roomTypeId) throws SQLException;

    /** Find rooms that still have available capacity (current_occupancy < max capacity). */
    List<Room> findAvailableRooms() throws SQLException;

    /** Update the current_occupancy counter for a room. */
    void updateOccupancy(int roomId, int newOccupancy) throws SQLException;
}
