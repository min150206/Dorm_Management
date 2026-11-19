package com.dorm.dao;

import com.dorm.model.Room;
import com.dorm.model.enums.RoomStatus;
import java.sql.SQLException;
import java.util.List;

public interface RoomDAO extends GenericDAO<Room, Integer> {

    List<Room> findByBuilding(String building) throws SQLException;

    List<Room> findByStatus(RoomStatus status) throws SQLException;

    List<Room> findByRoomTypeId(int roomTypeId) throws SQLException;

    // Returns rooms where current_occupancy < capacity
    List<Room> findAvailableRooms() throws SQLException;

    void updateOccupancy(int roomId, int newOccupancy) throws SQLException;
}
