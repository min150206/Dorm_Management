package com.dorm.dao;

import com.dorm.model.Request;
import com.dorm.model.enums.RequestStatus;
import java.sql.SQLException;
import java.util.List;

public interface RequestDAO extends GenericDAO<Request, Integer> {

    List<Request> findByStudentId(String studentId) throws SQLException;

    List<Request> findByRoomId(int roomId) throws SQLException;

    List<Request> findByStatus(RequestStatus status) throws SQLException;

    List<Request> findByResolvedBy(int accountId) throws SQLException;

    List<Request> findPendingRequests() throws SQLException;
}
