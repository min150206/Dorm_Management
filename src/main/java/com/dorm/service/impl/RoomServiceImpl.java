package com.dorm.service.impl;

import com.dorm.dao.RoomDAO;
import com.dorm.dao.RoomTypeDAO;
import com.dorm.dao.impl.RoomDAOImpl;
import com.dorm.dao.impl.RoomTypeDAOImpl;
import com.dorm.db.TransactionManager;
import com.dorm.exception.ServiceException;
import com.dorm.model.Room;
import com.dorm.model.RoomType;
import com.dorm.model.enums.RoomStatus;
import com.dorm.service.RoomService;
import com.dorm.util.Session;
import com.dorm.util.ValidationUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomServiceImpl implements RoomService {

    private final RoomDAO roomDao;
    private final RoomTypeDAO roomTypeDao;

    public RoomServiceImpl() {
        this(new RoomDAOImpl(), new RoomTypeDAOImpl());
    }

    public RoomServiceImpl(RoomDAO roomDao, RoomTypeDAO roomTypeDao) {
        this.roomDao = roomDao;
        this.roomTypeDao = roomTypeDao;
    }

    private void validateRoomType(RoomType type) {
        type.setTypeName(ValidationUtils.requireText(type.getTypeName(), "Room type name", 50));
        if (type.getCapacity() < 1 || type.getPrice() < 0) {
            throw new ServiceException("Capacity must be positive and price cannot be negative.");
        }
    }

    private RoomStatus computeRoomStatus(Room room, int capacity) {
        if (room.getRoomStatus() == RoomStatus.MAINTENANCE) {
            return RoomStatus.MAINTENANCE;
        }
        return room.getCurrentOccupancy() >= capacity ? RoomStatus.FULL : RoomStatus.AVAILABLE;
    }

    @Override
    public RoomType addRoomType(String typeName, int capacity, long price) {
        Session.requireManager();
        RoomType type = new RoomType(null, typeName, capacity, price);
        validateRoomType(type);
        try {
            return roomTypeDao.save(type);
        } catch (SQLException e) {
            throw new ServiceException("Failed to add room type: " + e.getMessage(), e);
        }
    }

    @Override
    public List<RoomType> getAllRoomTypes() {
        Session.requireLogin();
        try {
            return roomTypeDao.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch room types: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<RoomType> getRoomTypeById(int id) {
        Session.requireLogin();
        try {
            return roomTypeDao.findById(id);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch room type: " + e.getMessage(), e);
        }
    }

    @Override
    public RoomType updateRoomType(RoomType type) {
        Session.requireManager();
        validateRoomType(type);

        return TransactionManager.execute(() -> {
            for (Room room : roomDao.findByRoomTypeId(type.getId())) {
                if (room.getCurrentOccupancy() > type.getCapacity()) {
                    throw new ServiceException("Cannot reduce capacity below existing room occupancy.");
                }
                room.setRoomStatus(computeRoomStatus(room, type.getCapacity()));
                roomDao.update(room);
            }
            return roomTypeDao.update(type);
        });
    }

    @Override
    public Room addRoom(int roomTypeId, String roomNumber, String building) {
        Session.requireManager();
        String validNumber = ValidationUtils.requireText(roomNumber, "Room number", 20);
        String validBuilding = ValidationUtils.requireText(building, "Building", 20);

        try {
            if (!roomTypeDao.existsById(roomTypeId)) {
                throw new ServiceException("Room type does not exist.");
            }
            return roomDao.save(new Room(null, roomTypeId, validNumber, validBuilding, 0, RoomStatus.AVAILABLE));
        } catch (SQLException e) {
            throw new ServiceException("Failed to add room: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Room> getRoomById(int id) {
        Session.requireLogin();
        try {
            return roomDao.findById(id);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch room: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Room> getAllRooms() {
        Session.requireLogin();
        try {
            return roomDao.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch rooms: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Room> getRoomsByBuilding(String building) {
        Session.requireLogin();
        try {
            return roomDao.findByBuilding(building);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch rooms by building: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Room> getRoomsByBuilding(String building, RoomStatus status) {
        List<Room> result = new ArrayList<>();
        for (Room r : getRoomsByBuilding(building)) {
            if (status == null || r.getRoomStatus() == status) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public List<Room> getRoomsByStatus(RoomStatus status) {
        Session.requireLogin();
        try {
            return roomDao.findByStatus(status);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch rooms by status: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Room> getAvailableRooms() {
        Session.requireLogin();
        try {
            return roomDao.findAvailableRooms();
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch available rooms: " + e.getMessage(), e);
        }
    }

    @Override
    public Room updateRoom(Room room) {
        Session.requireManager();
        room.setBuilding(ValidationUtils.requireText(room.getBuilding(), "Building", 20));
        room.setRoomNumber(ValidationUtils.requireText(room.getRoomNumber(), "Room number", 20));
        if (room.getRoomStatus() == null) {
            throw new ServiceException("Room status is required.");
        }

        return TransactionManager.execute(() -> {
            Room current = roomDao.findById(room.getId())
                    .orElseThrow(() -> new ServiceException("Room not found."));
            RoomType type = roomTypeDao.findById(room.getRoomTypeId())
                    .orElseThrow(() -> new ServiceException("Room type not found."));

            room.setCurrentOccupancy(current.getCurrentOccupancy());
            if (room.getCurrentOccupancy() > type.getCapacity()) {
                throw new ServiceException("Current occupancy exceeds new room type capacity.");
            }
            room.setRoomStatus(computeRoomStatus(room, type.getCapacity()));
            return roomDao.update(room);
        });
    }

    @Override
    public boolean deleteRoom(int id) {
        Session.requireManager();
        try {
            return roomDao.deleteById(id);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete room: " + e.getMessage(), e);
        }
    }
}
