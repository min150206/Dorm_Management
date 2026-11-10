package com.dorm.logic.itf;

import com.dorm.model.Room;
import com.dorm.model.RoomType;
import com.dorm.model.enums.RoomStatus;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for room and room-type management.
 */
public interface RoomService {

    // ── Room Type operations ──────────────────────────────────────────

    RoomType addRoomType(String typeName, int capacity, long price) throws SQLException;

    List<RoomType> getAllRoomTypes() throws SQLException;

    Optional<RoomType> getRoomTypeById(int roomTypeId) throws SQLException;

    RoomType updateRoomType(RoomType roomType) throws SQLException;

    // ── Room operations ───────────────────────────────────────────────

    Room addRoom(int roomTypeId, String roomNumber, String building) throws SQLException;

    Optional<Room> getRoomById(int roomId) throws SQLException;

    List<Room> getAllRooms() throws SQLException;

    List<Room> getRoomsByBuilding(String building) throws SQLException;

    List<Room> getRoomsByBuilding(String building, RoomStatus status) throws SQLException;

    List<Room> getRoomsByStatus(RoomStatus status) throws SQLException;

    /** Find rooms that still have capacity for new students. */
    List<Room> getAvailableRooms() throws SQLException;

    Room updateRoom(Room room) throws SQLException;

    boolean deleteRoom(int roomId) throws SQLException;
}
