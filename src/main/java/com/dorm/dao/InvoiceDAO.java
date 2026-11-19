package com.dorm.dao;

import com.dorm.model.Invoice;
import com.dorm.model.enums.InvoiceStatus;
import java.sql.SQLException;
import java.util.List;

public interface InvoiceDAO extends GenericDAO<Invoice, Integer> {

    List<Invoice> findByContractId(int contractId) throws SQLException;

    List<Invoice> findByStatus(InvoiceStatus status) throws SQLException;

    List<Invoice> findUnpaidInvoices() throws SQLException;

    // Fetch unpaid invoices joined by student_id through contract
    List<Invoice> findUnpaidByStudentId(String studentId) throws SQLException;
}
