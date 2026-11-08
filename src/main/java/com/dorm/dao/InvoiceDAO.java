package com.dorm.dao;

import com.dorm.model.Invoice;
import com.dorm.model.enums.InvoiceStatus;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for the 'invoice' table.
 */
public interface InvoiceDAO extends GenericDAO<Invoice, Integer> {

    /** Find all invoices for a specific contract. */
    List<Invoice> findByContractId(int contractId) throws SQLException;

    /** Find all invoices with a given status. */
    List<Invoice> findByStatus(InvoiceStatus status) throws SQLException;

    /** Find all unpaid invoices across all contracts. */
    List<Invoice> findUnpaidInvoices() throws SQLException;

    /** Find all unpaid invoices for a specific student (joins through contract). */
    List<Invoice> findUnpaidByStudentId(String studentId) throws SQLException;
}
