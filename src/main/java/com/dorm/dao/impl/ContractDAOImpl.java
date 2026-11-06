package com.dorm.dao.impl;

import com.dorm.dao.ContractDAO;
import com.dorm.model.Contract;
import com.dorm.model.enums.ContractStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Concrete DAO implementation for the 'contract' table.
 */
public class ContractDAOImpl extends AbstractDAO<Contract, Integer> implements ContractDAO {

    // ── AbstractDAO template methods ──────────────────────────────────

    @Override
    protected String getTableName() {
        return "contract";
    }

    @Override
    protected String getIdColumnName() {
        return "contract_id";
    }

    @Override
    protected Contract mapRow(ResultSet rs) throws SQLException {
        return new Contract(
                rs.getInt("contract_id"),
                rs.getString("student_id"),
                rs.getInt("room_id"),
                rs.getDate("start_date"),
                rs.getDate("end_date"),
                ContractStatus.fromString(rs.getString("contract_status"))
        );
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO contract (student_id, room_id, start_date, end_date, contract_status) "
             + "VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE contract SET student_id = ?, room_id = ?, start_date = ?, "
             + "end_date = ?, contract_status = ? WHERE contract_id = ?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Contract entity) throws SQLException {
        ps.setString(1, entity.getStudentId());
        ps.setInt(2, entity.getRoomId());
        ps.setDate(3, entity.getStartDate());
        ps.setDate(4, entity.getEndDate());
        ps.setString(5, entity.getContractStatus().name());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Contract entity) throws SQLException {
        ps.setString(1, entity.getStudentId());
        ps.setInt(2, entity.getRoomId());
        ps.setDate(3, entity.getStartDate());
        ps.setDate(4, entity.getEndDate());
        ps.setString(5, entity.getContractStatus().name());
        ps.setInt(6, entity.getContractId());
    }

    // ── ContractDAO-specific methods ──────────────────────────────────

    @Override
    public List<Contract> findByStudentId(String studentId) throws SQLException {
        return queryList("SELECT * FROM contract WHERE student_id = ? ORDER BY start_date DESC",
                studentId);
    }

    @Override
    public List<Contract> findByRoomId(int roomId) throws SQLException {
        return queryList("SELECT * FROM contract WHERE room_id = ? ORDER BY start_date DESC",
                roomId);
    }

    @Override
    public List<Contract> findByStatus(ContractStatus status) throws SQLException {
        return queryList("SELECT * FROM contract WHERE contract_status = ?", status.name());
    }

    @Override
    public List<Contract> findActiveContracts() throws SQLException {
        return queryList(
            "SELECT * FROM contract WHERE contract_status = 'ACTIVE' ORDER BY start_date DESC"
        );
    }

    @Override
    public List<Contract> findActiveByStudentId(String studentId) throws SQLException {
        return queryList(
            "SELECT * FROM contract WHERE student_id = ? AND contract_status = 'ACTIVE' "
          + "ORDER BY start_date DESC",
            studentId
        );
    }
}
