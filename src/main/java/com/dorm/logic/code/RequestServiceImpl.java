package com.dorm.logic.code;

import com.dorm.dao.impl.*;
import com.dorm.model.*;
import com.dorm.model.enums.*;
import com.dorm.logic.itf.*;
import java.sql.*;
import java.util.*;

public class RequestServiceImpl implements RequestService {
    private final RequestDAOImpl requests = new RequestDAOImpl();
    public Request submitRequest(String studentId, int roomId, String title, String description) throws SQLException {
        ServiceSupport.ownStudent(studentId);
        String heading = ServiceSupport.text(title, "Title", 255);
        String detail = ServiceSupport.text(description, "Description", 4000);
        boolean livingHere = new ContractDAOImpl().findActiveByStudentId(studentId).stream()
                .anyMatch(c -> c.getRoomId() == roomId && !c.getEndDate().toLocalDate().isBefore(java.time.LocalDate.now()));
        if (!livingHere) throw new IllegalArgumentException("Student needs an active contract for this room.");
        return requests.save(new Request(null, studentId, roomId, heading, detail,
                RequestStatus.PENDING, new Timestamp(System.currentTimeMillis()), null, null));
    }
    public Optional<Request> getRequestById(int id) throws SQLException {
        Session.requireLogin(); Optional<Request> r = requests.findById(id);
        if (r.isPresent()) ServiceSupport.ownStudent(r.get().getStudentId()); return r;
    }
    public List<Request> getRequestsByStudent(String id) throws SQLException { ServiceSupport.ownStudent(id); return requests.findByStudentId(id); }
    public List<Request> getRequestsByRoom(int id) throws SQLException { Session.requireManager(); return requests.findByRoomId(id); }
    public List<Request> getRequestsByStatus(RequestStatus s) throws SQLException { Session.requireManager(); return requests.findByStatus(s); }
    public List<Request> getPendingRequests() throws SQLException { Session.requireManager(); return requests.findPendingRequests(); }
    public List<Request> getAllRequests() throws SQLException { Session.requireManager(); return requests.findAll(); }
    public Request resolveRequest(int id, int resolver, RequestStatus status) throws SQLException {
        Session.requireManager();
        if (resolver != Session.getAccount().getId()) throw new IllegalArgumentException("Resolver must be the signed-in user.");
        if (status != RequestStatus.PROCESSING && status != RequestStatus.RESOLVED)
            throw new IllegalArgumentException("Choose PROCESSING or RESOLVED.");
        return ServiceSupport.transaction(() -> {
            ServiceSupport.lock("SELECT request_id FROM request WHERE request_id=? FOR UPDATE", id);
            Request r = requests.findById(id).orElseThrow();
            if (r.getRequestStatus() == RequestStatus.RESOLVED) throw new IllegalArgumentException("Request is already resolved.");
            r.setRequestStatus(status);
            r.setResolvedTime(status == RequestStatus.RESOLVED ? new Timestamp(System.currentTimeMillis()) : null);
            r.setResolvedBy(status == RequestStatus.RESOLVED ? resolver : null);
            return requests.update(r);
        });
    }
}
