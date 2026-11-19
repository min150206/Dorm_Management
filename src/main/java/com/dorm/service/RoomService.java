package com.dorm.service;

import com.dorm.model.Room;
import com.dorm.model.RoomType;
import com.dorm.model.enums.RoomStatus;
import java.util.List;
import java.util.Optional;

public interface RoomService {

    RoomType addRoomType(String typeName, int capacity, long price);

    List<RoomType> getAllRoomTypes();

    Optional<RoomType> getRoomTypeById(int roomTypeId);

    RoomType updateRoomType(RoomType roomType);

    Room addRoom(int roomTypeId, String roomNumber, String building);

    Optional<Room> getRoomById(int roomId);

    List<Room> getAllRooms();

    List<Room> getRoomsByBuilding(String building);

    List<Room> getRoomsByBuilding(String building, RoomStatus status);

    List<Room> getRoomsByStatus(RoomStatus status);

    List<Room> getAvailableRooms();

    Room updateRoom(Room room);

    boolean deleteRoom(int roomId);
}
