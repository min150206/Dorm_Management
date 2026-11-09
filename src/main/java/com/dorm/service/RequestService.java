package com.dorm.service;

import com.dorm.model.Request;
import com.dorm.model.enums.RequestStatus;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for maintenance/service request management.
 */
public interface RequestService {

    /** Submit a new request from a student for a specific room. */
    Request submitRequest(String studentId, int roomId,
                          String title, String description) throws SQLException;

    Optional<Request> getRequestById(int requestId) throws SQLException;

    List<Request> getRequestsByStudent(String studentId) throws SQLException;

    List<Request> getRequestsByRoom(int roomId) throws SQLException;

    List<Request> getRequestsByStatus(RequestStatus status) throws SQLException;

    List<Request> getPendingRequests() throws SQLException;

    /** Resolve a request — sets status, resolved_time, and resolved_by (admin account). */
    Request resolveRequest(int requestId, int adminAccountId, RequestStatus newStatus) throws SQLException;
}
