package com.dorm.service;

import com.dorm.model.Contract;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

public interface ContractService {

    Contract createContract(String studentId, int roomId, Date startDate, Date endDate);

    Optional<Contract> getContractById(int contractId);

    List<Contract> getContractsByStudent(String studentId);

    List<Contract> getContractsByRoom(int roomId);

    List<Contract> getActiveContracts();

    List<Contract> getAllContracts();

    Contract cancelContract(int contractId);

    Contract expireContract(int contractId);

    int expireOldContracts();
}
