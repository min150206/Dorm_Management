package com.dorm.dao;

import com.dorm.model.Contract;
import com.dorm.model.enums.ContractStatus;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for the 'contract' table.
 */
public interface ContractDAO extends GenericDAO<Contract, Integer> {

    /** Find all contracts for a specific student. */
    List<Contract> findByStudentId(String studentId) throws SQLException;

    /** Find all contracts assigned to a specific room. */
    List<Contract> findByRoomId(int roomId) throws SQLException;

    /** Find all contracts with a given status. */
    List<Contract> findByStatus(ContractStatus status) throws SQLException;

    /** Find all currently active contracts. */
    List<Contract> findActiveContracts() throws SQLException;

    /** Find the active contract for a given student (if any). */
    List<Contract> findActiveByStudentId(String studentId) throws SQLException;
}
