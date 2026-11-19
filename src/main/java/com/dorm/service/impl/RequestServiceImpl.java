package com.dorm.service.impl;

import com.dorm.dao.ContractDAO;
import com.dorm.dao.RequestDAO;
import com.dorm.dao.StudentDAO;
import com.dorm.dao.impl.ContractDAOImpl;
import com.dorm.dao.impl.RequestDAOImpl;
import com.dorm.dao.impl.StudentDAOImpl;
import com.dorm.db.TransactionManager;
import com.dorm.exception.ServiceException;
import com.dorm.model.Request;
import com.dorm.model.enums.RequestStatus;
import com.dorm.service.RequestService;
import com.dorm.util.Session;
import com.dorm.util.ValidationUtils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class RequestServiceImpl implements RequestService {

    private final RequestDAO requestDao;
    private final ContractDAO contractDao;
    private final StudentDAO studentDao;

    public RequestServiceImpl() {
        this(new RequestDAOImpl(), new ContractDAOImpl(), new StudentDAOImpl());
    }

    public RequestServiceImpl(RequestDAO requestDao, ContractDAO contractDao, StudentDAO studentDao) {
        this.requestDao = requestDao;
        this.contractDao = contractDao;
        this.studentDao = studentDao;
    }

    @Override
    public Request submitRequest(String studentId, int roomId, String title, String description) {
        ValidationUtils.checkStudentOwnership(studentId, studentDao);
        String validTitle = ValidationUtils.requireText(title, "Title", 255);
        String validDesc = ValidationUtils.requireText(description, "Description", 4000);

        try {
            boolean hasActiveRoomContract = contractDao.findActiveByStudentId(studentId).stream()
                    .anyMatch(c -> c.getRoomId() == roomId && !c.getEndDate().toLocalDate().isBefore(LocalDate.now()));
            if (!hasActiveRoomContract) {
                throw new ServiceException("Student does not have an active contract for this room.");
            }

            return requestDao.save(new Request(null, studentId, roomId, validTitle, validDesc,
                    RequestStatus.PENDING, new Timestamp(System.currentTimeMillis()), null, null));
        } catch (SQLException e) {
            throw new ServiceException("Failed to submit request: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Request> getRequestById(int id) {
        Session.requireLogin();
        try {
            Optional<Request> request = requestDao.findById(id);
            request.ifPresent(r -> ValidationUtils.checkStudentOwnership(r.getStudentId(), studentDao));
            return request;
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch request: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Request> getRequestsByStudent(String studentId) {
        ValidationUtils.checkStudentOwnership(studentId, studentDao);
        try {
            return requestDao.findByStudentId(studentId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch student requests: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Request> getRequestsByRoom(int roomId) {
        Session.requireManager();
        try {
            return requestDao.findByRoomId(roomId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch room requests: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Request> getRequestsByStatus(RequestStatus status) {
        Session.requireManager();
        try {
            return requestDao.findByStatus(status);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch requests by status: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Request> getPendingRequests() {
        Session.requireManager();
        try {
            return requestDao.findPendingRequests();
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch pending requests: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Request> getAllRequests() {
        Session.requireManager();
        try {
            return requestDao.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch all requests: " + e.getMessage(), e);
        }
    }

    @Override
    public Request resolveRequest(int id, int resolverAccountId, RequestStatus status) {
        Session.requireManager();
        if (resolverAccountId != Session.getAccount().getId()) {
            throw new ServiceException("Resolver ID must match currently logged-in account.");
        }
        if (status != RequestStatus.PROCESSING && status != RequestStatus.RESOLVED) {
            throw new ServiceException("Status must be updated to PROCESSING or RESOLVED.");
        }

        return TransactionManager.execute(() -> {
            Request request = requestDao.findById(id)
                    .orElseThrow(() -> new ServiceException("Request not found."));
            if (request.getRequestStatus() == RequestStatus.RESOLVED) {
                throw new ServiceException("Request is already marked as resolved.");
            }
            request.setRequestStatus(status);
            request.setResolvedTime(status == RequestStatus.RESOLVED ? new Timestamp(System.currentTimeMillis()) : null);
            request.setResolvedBy(status == RequestStatus.RESOLVED ? resolverAccountId : null);
            return requestDao.update(request);
        });
    }
}
