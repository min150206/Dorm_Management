package com.dorm.dao.impl;

import com.dorm.dao.RequestDAO;
import com.dorm.model.Request;
import com.dorm.model.enums.RequestStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

/**
 * Concrete DAO implementation for the 'request' table.
 */
public class RequestDAOImpl extends AbstractDAO<Request, Integer> implements RequestDAO {

    // ── AbstractDAO template methods ──────────────────────────────────

    @Override
    protected String getTableName() {
        return "request";
    }

    @Override
    protected String getIdColumnName() {
        return "request_id";
    }

    @Override
    protected Request mapRow(ResultSet rs) throws SQLException {
        // resolved_by is nullable
        int resolvedByRaw = rs.getInt("resolved_by");
        Integer resolvedBy = rs.wasNull() ? null : resolvedByRaw;

        return new Request(
                rs.getInt("request_id"),
                rs.getString("student_id"),
                rs.getInt("room_id"),
                rs.getString("title"),
                rs.getString("description"),
                RequestStatus.fromString(rs.getString("request_status")),
                rs.getTimestamp("requested_time"),
                rs.getTimestamp("resolved_time"),
                resolvedBy
        );
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO request (student_id, room_id, title, description, "
             + "request_status, requested_time, resolved_time, resolved_by) "
             + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE request SET student_id = ?, room_id = ?, title = ?, description = ?, "
             + "request_status = ?, requested_time = ?, resolved_time = ?, resolved_by = ? "
             + "WHERE request_id = ?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Request entity) throws SQLException {
        ps.setString(1, entity.getStudentId());
        ps.setInt(2, entity.getRoomId());
        ps.setString(3, entity.getTitle());
        ps.setString(4, entity.getDescription());
        ps.setString(5, entity.getRequestStatus().name());
        ps.setTimestamp(6, entity.getRequestedTime() != null
                ? entity.getRequestedTime()
                : new Timestamp(System.currentTimeMillis()));
        ps.setTimestamp(7, entity.getResolvedTime());   // nullable
        if (entity.getResolvedBy() != null) {
            ps.setInt(8, entity.getResolvedBy());
        } else {
            ps.setNull(8, Types.INTEGER);
        }
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Request entity) throws SQLException {
        ps.setString(1, entity.getStudentId());
        ps.setInt(2, entity.getRoomId());
        ps.setString(3, entity.getTitle());
        ps.setString(4, entity.getDescription());
        ps.setString(5, entity.getRequestStatus().name());
        ps.setTimestamp(6, entity.getRequestedTime());
        ps.setTimestamp(7, entity.getResolvedTime());
        if (entity.getResolvedBy() != null) {
            ps.setInt(8, entity.getResolvedBy());
        } else {
            ps.setNull(8, Types.INTEGER);
        }
        ps.setInt(9, entity.getRequestId());
    }

    // ── RequestDAO-specific methods ───────────────────────────────────

    @Override
    public List<Request> findByStudentId(String studentId) throws SQLException {
        return queryList("SELECT * FROM request WHERE student_id = ? ORDER BY requested_time DESC",
                studentId);
    }

    @Override
    public List<Request> findByRoomId(int roomId) throws SQLException {
        return queryList("SELECT * FROM request WHERE room_id = ? ORDER BY requested_time DESC",
                roomId);
    }

    @Override
    public List<Request> findByStatus(RequestStatus status) throws SQLException {
        return queryList("SELECT * FROM request WHERE request_status = ? ORDER BY requested_time DESC",
                status.name());
    }

    @Override
    public List<Request> findByResolvedBy(int accountId) throws SQLException {
        return queryList("SELECT * FROM request WHERE resolved_by = ? ORDER BY resolved_time DESC",
                accountId);
    }

    @Override
    public List<Request> findPendingRequests() throws SQLException {
        return queryList(
            "SELECT * FROM request WHERE request_status = 'PENDING' ORDER BY requested_time ASC"
        );
    }
}
