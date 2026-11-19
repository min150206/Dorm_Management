package com.dorm.service.impl;

import com.dorm.dao.ContractDAO;
import com.dorm.dao.RoomDAO;
import com.dorm.dao.RoomTypeDAO;
import com.dorm.dao.StudentDAO;
import com.dorm.dao.impl.ContractDAOImpl;
import com.dorm.dao.impl.RoomDAOImpl;
import com.dorm.dao.impl.RoomTypeDAOImpl;
import com.dorm.dao.impl.StudentDAOImpl;
import com.dorm.db.TransactionManager;
import com.dorm.exception.ServiceException;
import com.dorm.model.Contract;
import com.dorm.model.Room;
import com.dorm.model.RoomType;
import com.dorm.model.enums.ContractStatus;
import com.dorm.model.enums.RoomStatus;
import com.dorm.service.ContractService;
import com.dorm.util.Session;
import com.dorm.util.ValidationUtils;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ContractServiceImpl implements ContractService {

    private final ContractDAO contractDao;
    private final RoomDAO roomDao;
    private final RoomTypeDAO roomTypeDao;
    private final StudentDAO studentDao;

    public ContractServiceImpl() {
        this(new ContractDAOImpl(), new RoomDAOImpl(), new RoomTypeDAOImpl(), new StudentDAOImpl());
    }

    public ContractServiceImpl(ContractDAO contractDao, RoomDAO roomDao, RoomTypeDAO roomTypeDao, StudentDAO studentDao) {
        this.contractDao = contractDao;
        this.roomDao = roomDao;
        this.roomTypeDao = roomTypeDao;
        this.studentDao = studentDao;
    }

    @Override
    public Contract createContract(String studentId, int roomId, Date start, Date end) {
        Session.requireManager();
        if (start == null || end == null || !end.after(start) || end.toLocalDate().isBefore(LocalDate.now())) {
            throw new ServiceException("End date must follow start date and cannot be in the past.");
        }
        if (start.toLocalDate().isAfter(LocalDate.now())) {
            throw new ServiceException("Immediate allocation only; start date cannot be in the future.");
        }

        return TransactionManager.execute(() -> {
            if (!studentDao.existsById(studentId)) {
                throw new ServiceException("Student record not found.");
            }
            if (!contractDao.findActiveByStudentId(studentId).isEmpty()) {
                throw new ServiceException("Student already has an active contract.");
            }

            Room room = roomDao.findById(roomId)
                    .orElseThrow(() -> new ServiceException("Room not found."));
            RoomType roomType = roomTypeDao.findById(room.getRoomTypeId())
                    .orElseThrow(() -> new ServiceException("Room type not found."));

            if (room.getRoomStatus() == RoomStatus.MAINTENANCE || room.getCurrentOccupancy() >= roomType.getCapacity()) {
                throw new ServiceException("Room is full or currently under maintenance.");
            }

            Contract contract = contractDao.save(new Contract(null, studentId, roomId, start, end, ContractStatus.ACTIVE));
            room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
            room.setRoomStatus(room.getCurrentOccupancy() >= roomType.getCapacity() ? RoomStatus.FULL : RoomStatus.AVAILABLE);
            roomDao.update(room);
            return contract;
        });
    }

    @Override
    public Optional<Contract> getContractById(int id) {
        Session.requireLogin();
        try {
            Optional<Contract> contract = contractDao.findById(id);
            contract.ifPresent(c -> ValidationUtils.checkStudentOwnership(c.getStudentId(), studentDao));
            return contract;
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch contract: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Contract> getContractsByStudent(String studentId) {
        ValidationUtils.checkStudentOwnership(studentId, studentDao);
        try {
            return contractDao.findByStudentId(studentId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch student contracts: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Contract> getContractsByRoom(int roomId) {
        Session.requireManager();
        try {
            return contractDao.findByRoomId(roomId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch room contracts: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Contract> getActiveContracts() {
        Session.requireManager();
        try {
            return contractDao.findActiveContracts();
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch active contracts: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Contract> getAllContracts() {
        Session.requireManager();
        try {
            return contractDao.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch contracts: " + e.getMessage(), e);
        }
    }

    @Override
    public Contract cancelContract(int id) {
        return terminateContract(id, ContractStatus.TERMINATED);
    }

    @Override
    public Contract expireContract(int id) {
        return terminateContract(id, ContractStatus.EXPIRED);
    }

    private Contract terminateContract(int id, ContractStatus status) {
        Session.requireManager();
        return TransactionManager.execute(() -> {
            Contract contract = contractDao.findById(id)
                    .orElseThrow(() -> new ServiceException("Contract not found."));

            if (contract.getContractStatus() != ContractStatus.ACTIVE) {
                return contract;
            }
            if (status == ContractStatus.EXPIRED && !contract.getEndDate().toLocalDate().isBefore(LocalDate.now())) {
                throw new ServiceException("Contract has not passed its expiration date.");
            }

            Room room = roomDao.findById(contract.getRoomId())
                    .orElseThrow(() -> new ServiceException("Associated room not found."));

            contract.setContractStatus(status);
            contractDao.update(contract);

            room.setCurrentOccupancy(Math.max(0, room.getCurrentOccupancy() - 1));
            if (room.getRoomStatus() != RoomStatus.MAINTENANCE) {
                room.setRoomStatus(RoomStatus.AVAILABLE);
            }
            roomDao.update(room);
            return contract;
        });
    }

    @Override
    public int expireOldContracts() {
        Session.requireManager();
        try {
            int count = 0;
            for (Contract c : contractDao.findActiveContracts()) {
                if (c.getEndDate().toLocalDate().isBefore(LocalDate.now())) {
                    expireContract(c.getId());
                    count++;
                }
            }
            return count;
        } catch (SQLException e) {
            throw new ServiceException("Failed to process expired contracts: " + e.getMessage(), e);
        }
    }
}
