package com.dorm.dao.impl;

import com.dorm.dao.InvoiceDAO;
import com.dorm.model.Invoice;
import com.dorm.model.enums.InvoiceStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Concrete DAO implementation for the 'invoice' table.
 */
public class InvoiceDAOImpl extends AbstractDAO<Invoice, Integer> implements InvoiceDAO {

    // ── AbstractDAO template methods ──────────────────────────────────

    @Override
    protected String getTableName() {
        return "invoice";
    }

    @Override
    protected String getIdColumnName() {
        return "invoice_id";
    }

    @Override
    protected Invoice mapRow(ResultSet rs) throws SQLException {
        return new Invoice(
                rs.getInt("invoice_id"),
                rs.getInt("contract_id"),
                rs.getString("title"),
                rs.getLong("fee"),
                InvoiceStatus.fromString(rs.getString("invoice_status")),
                rs.getTimestamp("paid_time")
        );
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO invoice (contract_id, title, fee, invoice_status, paid_time) "
             + "VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE invoice SET contract_id = ?, title = ?, fee = ?, "
             + "invoice_status = ?, paid_time = ? WHERE invoice_id = ?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Invoice entity) throws SQLException {
        ps.setInt(1, entity.getContractId());
        ps.setString(2, entity.getTitle());
        ps.setLong(3, entity.getFee());
        ps.setString(4, entity.getInvoiceStatus().name());
        ps.setTimestamp(5, entity.getPaidTime());   // nullable
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Invoice entity) throws SQLException {
        ps.setInt(1, entity.getContractId());
        ps.setString(2, entity.getTitle());
        ps.setLong(3, entity.getFee());
        ps.setString(4, entity.getInvoiceStatus().name());
        ps.setTimestamp(5, entity.getPaidTime());
        ps.setInt(6, entity.getInvoiceId());
    }

    // ── InvoiceDAO-specific methods ───────────────────────────────────

    @Override
    public List<Invoice> findByContractId(int contractId) throws SQLException {
        return queryList("SELECT * FROM invoice WHERE contract_id = ?", contractId);
    }

    @Override
    public List<Invoice> findByStatus(InvoiceStatus status) throws SQLException {
        return queryList("SELECT * FROM invoice WHERE invoice_status = ?", status.name());
    }

    @Override
    public List<Invoice> findUnpaidInvoices() throws SQLException {
        return queryList("SELECT * FROM invoice WHERE invoice_status = 'UNPAID'");
    }

    @Override
    public List<Invoice> findUnpaidByStudentId(String studentId) throws SQLException {
        return queryList(
            "SELECT i.* FROM invoice i "
          + "JOIN contract c ON i.contract_id = c.contract_id "
          + "WHERE c.student_id = ? AND i.invoice_status = 'UNPAID'",
            studentId
        );
    }
}
