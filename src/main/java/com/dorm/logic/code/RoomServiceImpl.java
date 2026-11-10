package com.dorm.logic.code;

import com.dorm.dao.impl.*;
import com.dorm.model.*;
import com.dorm.model.enums.RoomStatus;
import com.dorm.logic.itf.*;
import java.sql.SQLException;
import java.util.*;

public class RoomServiceImpl implements RoomService {
    private final RoomDAOImpl rooms = new RoomDAOImpl();
    private final RoomTypeDAOImpl types = new RoomTypeDAOImpl();
    public RoomType addRoomType(String name, int capacity, long price) throws SQLException {
        Session.requireManager();
        RoomType t = new RoomType(null, name, capacity, price); validateType(t); return types.save(t);
    }
    private void validateType(RoomType t) {
        t.setTypeName(ServiceSupport.text(t.getTypeName(), "Type name", 50));
        if (t.getCapacity() < 1 || t.getPrice() < 0) throw new IllegalArgumentException("Capacity must be positive and price cannot be negative.");
    }
    public List<RoomType> getAllRoomTypes() throws SQLException { Session.requireLogin(); return types.findAll(); }
    public Optional<RoomType> getRoomTypeById(int id) throws SQLException { Session.requireLogin(); return types.findById(id); }
    public RoomType updateRoomType(RoomType t) throws SQLException {
        Session.requireManager(); validateType(t);
        return ServiceSupport.transaction(() -> {
            ServiceSupport.lock("SELECT room_type_id FROM room_type WHERE room_type_id=? FOR UPDATE", t.getId());
            for (Room r : rooms.findByRoomTypeId(t.getId())) {
                ServiceSupport.lock("SELECT room_id FROM room WHERE room_id=? FOR UPDATE", r.getId());
                r = rooms.findById(r.getId()).orElseThrow();
                if (r.getCurrentOccupancy() > t.getCapacity()) throw new IllegalArgumentException("Capacity is below existing occupancy.");
                r.setRoomStatus(status(r, t.getCapacity())); rooms.update(r);
            }
            return types.update(t);
        });
    }
    private RoomStatus status(Room r, int capacity) {
        if (r.getRoomStatus() == RoomStatus.MAINTENANCE) return RoomStatus.MAINTENANCE;
        return r.getCurrentOccupancy() >= capacity ? RoomStatus.FULL : RoomStatus.AVAILABLE;
    }
    public Room addRoom(int type, String number, String building) throws SQLException {
        Session.requireManager();
        if (types.findById(type).isEmpty()) throw new IllegalArgumentException("Room type does not exist.");
        return rooms.save(new Room(null, type, ServiceSupport.text(number, "Room number", 20),
                ServiceSupport.text(building, "Building", 20), 0, RoomStatus.AVAILABLE));
    }
    public Optional<Room> getRoomById(int id) throws SQLException { Session.requireLogin(); return rooms.findById(id); }
    public List<Room> getAllRooms() throws SQLException { Session.requireLogin(); return rooms.findAll(); }
    public List<Room> getRoomsByBuilding(String b) throws SQLException { Session.requireLogin(); return rooms.findByBuilding(b); }
    public List<Room> getRoomsByBuilding(String building, RoomStatus status) throws SQLException {
        List<Room> result = new ArrayList<>();
        for (Room r : getRoomsByBuilding(building)) {
            if (status == null || r.getRoomStatus() == status) result.add(r);
        }
        return result;
    }
    public List<Room> getRoomsByStatus(RoomStatus s) throws SQLException { Session.requireLogin(); return rooms.findByStatus(s); }
    public List<Room> getAvailableRooms() throws SQLException { Session.requireLogin(); return rooms.findAvailableRooms(); }
    public Room updateRoom(Room r) throws SQLException {
        Session.requireManager();
        r.setBuilding(ServiceSupport.text(r.getBuilding(), "Building", 20));
        r.setRoomNumber(ServiceSupport.text(r.getRoomNumber(), "Room number", 20));
        if (r.getRoomStatus() == null) throw new IllegalArgumentException("Choose status.");
        return ServiceSupport.transaction(() -> {
            ServiceSupport.lock("SELECT room_type_id FROM room_type WHERE room_type_id=? FOR UPDATE", r.getRoomTypeId());
            ServiceSupport.lock("SELECT room_id FROM room WHERE room_id=? FOR UPDATE", r.getId());
            Room old = rooms.findById(r.getId()).orElseThrow();
            int capacity = types.findById(r.getRoomTypeId()).orElseThrow().getCapacity();
            r.setCurrentOccupancy(old.getCurrentOccupancy());
            if (r.getCurrentOccupancy() > capacity) throw new IllegalArgumentException("Room type is too small.");
            r.setRoomStatus(status(r, capacity)); return rooms.update(r);
        });
    }
    public boolean deleteRoom(int id) throws SQLException { Session.requireManager(); return rooms.deleteById(id); }
}
