package com.dorm.dao;

import com.dorm.model.Request;
import com.dorm.model.enums.RequestStatus;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for the 'request' table.
 */
public interface RequestDAO extends GenericDAO<Request, Integer> {

    /** Find all requests submitted by a specific student. */
    List<Request> findByStudentId(String studentId) throws SQLException;

    /** Find all requests associated with a specific room. */
    List<Request> findByRoomId(int roomId) throws SQLException;

    /** Find all requests with a given status. */
    List<Request> findByStatus(RequestStatus status) throws SQLException;

    /** Find all requests resolved by a specific admin account. */
    List<Request> findByResolvedBy(int accountId) throws SQLException;

    /** Find all pending (unresolved) requests. */
    List<Request> findPendingRequests() throws SQLException;
}
