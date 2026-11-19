package com.dorm.service;

import com.dorm.model.Request;
import com.dorm.model.enums.RequestStatus;
import java.util.List;
import java.util.Optional;

public interface RequestService {

    Request submitRequest(String studentId, int roomId, String title, String description);

    Optional<Request> getRequestById(int requestId);

    List<Request> getRequestsByStudent(String studentId);

    List<Request> getRequestsByRoom(int roomId);

    List<Request> getRequestsByStatus(RequestStatus status);

    List<Request> getPendingRequests();

    List<Request> getAllRequests();

    Request resolveRequest(int requestId, int adminAccountId, RequestStatus newStatus);
}
