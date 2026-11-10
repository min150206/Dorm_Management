package com.dorm.logic.code;

import com.dorm.dao.impl.*;
import com.dorm.model.*;
import com.dorm.model.enums.*;
import com.dorm.logic.itf.*;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class ContractServiceImpl implements ContractService {
    private final ContractDAOImpl contracts = new ContractDAOImpl();
    private final RoomDAOImpl rooms = new RoomDAOImpl();
    private final RoomTypeDAOImpl types = new RoomTypeDAOImpl();

    public Contract createContract(String studentId, int roomId, Date start, Date end) throws SQLException {
        Session.requireManager();
        if (start == null || end == null || !end.after(start) || end.toLocalDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("End date must follow start date and cannot be in the past.");
        if (start.toLocalDate().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("This version allocates rooms immediately; start date cannot be in the future.");
        return ServiceSupport.transaction(() -> {
            ServiceSupport.lock("SELECT student_id FROM student WHERE student_id=? FOR UPDATE", studentId);
            if (!contracts.findActiveByStudentId(studentId).isEmpty())
                throw new IllegalArgumentException("Student already has an active contract.");
            Room before = rooms.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Room missing."));
            ServiceSupport.lock("SELECT room_type_id FROM room_type WHERE room_type_id=? FOR UPDATE", before.getRoomTypeId());
            ServiceSupport.lock("SELECT room_id FROM room WHERE room_id=? FOR UPDATE", roomId);
            Room room = rooms.findById(roomId).orElseThrow();
            int capacity = types.findById(room.getRoomTypeId()).orElseThrow().getCapacity();
            if (room.getRoomStatus() == RoomStatus.MAINTENANCE || room.getCurrentOccupancy() >= capacity)
                throw new IllegalArgumentException("Room is full or under maintenance.");
            Contract contract = contracts.save(new Contract(null, studentId, roomId, start, end, ContractStatus.ACTIVE));
            room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
            room.setRoomStatus(room.getCurrentOccupancy() == capacity ? RoomStatus.FULL : RoomStatus.AVAILABLE);
            rooms.update(room);
            return contract;
        });
    }
    public Optional<Contract> getContractById(int id) throws SQLException {
        Session.requireLogin(); Optional<Contract> c = contracts.findById(id);
        if (c.isPresent()) ServiceSupport.ownStudent(c.get().getStudentId()); return c;
    }
    public List<Contract> getContractsByStudent(String id) throws SQLException {
        ServiceSupport.ownStudent(id); return contracts.findByStudentId(id);
    }
    public List<Contract> getContractsByRoom(int id) throws SQLException { Session.requireManager(); return contracts.findByRoomId(id); }
    public List<Contract> getActiveContracts() throws SQLException { Session.requireManager(); return contracts.findActiveContracts(); }
    public List<Contract> getAllContracts() throws SQLException { Session.requireManager(); return contracts.findAll(); }
    public Contract cancelContract(int id) throws SQLException { return finish(id, ContractStatus.TERMINATED); }
    public Contract expireContract(int id) throws SQLException { return finish(id, ContractStatus.EXPIRED); }
    private Contract finish(int id, ContractStatus status) throws SQLException {
        Session.requireManager();
        return ServiceSupport.transaction(() -> {
            ServiceSupport.lock("SELECT contract_id FROM contract WHERE contract_id=? FOR UPDATE", id);
            Contract c = contracts.findById(id).orElseThrow();
            if (c.getContractStatus() != ContractStatus.ACTIVE) return c; // no second occupancy decrement
            if (status == ContractStatus.EXPIRED && !c.getEndDate().toLocalDate().isBefore(LocalDate.now()))
                throw new IllegalArgumentException("Contract has not passed its end date.");
            ServiceSupport.lock("SELECT room_id FROM room WHERE room_id=? FOR UPDATE", c.getRoomId());
            Room r = rooms.findById(c.getRoomId()).orElseThrow();
            c.setContractStatus(status); contracts.update(c);
            r.setCurrentOccupancy(Math.max(0, r.getCurrentOccupancy() - 1));
            if (r.getRoomStatus() != RoomStatus.MAINTENANCE) r.setRoomStatus(RoomStatus.AVAILABLE);
            rooms.update(r); return c;
        });
    }
    public int expireOldContracts() throws SQLException {
        Session.requireManager(); int count = 0;
        for (Contract c : contracts.findActiveContracts()) {
            if (c.getEndDate().toLocalDate().isBefore(LocalDate.now())) { expireContract(c.getId()); count++; }
        }
        return count;
    }
}
