package com.dorm.dao;

import com.dorm.model.Contract;
import com.dorm.model.enums.ContractStatus;
import java.sql.SQLException;
import java.util.List;

public interface ContractDAO extends GenericDAO<Contract, Integer> {

    List<Contract> findByStudentId(String studentId) throws SQLException;

    List<Contract> findByRoomId(int roomId) throws SQLException;

    List<Contract> findByStatus(ContractStatus status) throws SQLException;

    List<Contract> findActiveContracts() throws SQLException;

    List<Contract> findActiveByStudentId(String studentId) throws SQLException;
}
