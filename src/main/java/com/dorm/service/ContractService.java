package com.dorm.service;

import com.dorm.model.Contract;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for contract lifecycle management.
 */
public interface ContractService {

    /**
     * Create a new contract, assigning a student to a room.
     * Automatically increments the room's current_occupancy and
     * updates room status to FULL if capacity is reached.
     */
    Contract createContract(String studentId, int roomId,
                            Date startDate, Date endDate) throws SQLException;

    Optional<Contract> getContractById(int contractId) throws SQLException;

    List<Contract> getContractsByStudent(String studentId) throws SQLException;

    List<Contract> getContractsByRoom(int roomId) throws SQLException;

    List<Contract> getActiveContracts() throws SQLException;

    /**
     * Cancel a contract.
     * Decrements the room's current_occupancy and updates room status accordingly.
     */
    Contract cancelContract(int contractId) throws SQLException;

    /**
     * Expire a contract (e.g. when end_date passes).
     * Decrements the room's current_occupancy.
     */
    Contract expireContract(int contractId) throws SQLException;
}
