package com.dorm.logic.itf;

import com.dorm.model.Invoice;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for invoice / billing management.
 */
public interface InvoiceService {

    /** Generate a new invoice for a contract. */
    Invoice createInvoice(int contractId, String title, long fee) throws SQLException;

    Optional<Invoice> getInvoiceById(int invoiceId) throws SQLException;

    List<Invoice> getInvoicesByContract(int contractId) throws SQLException;

    /** Get all unpaid invoices system-wide. */
    List<Invoice> getUnpaidInvoices() throws SQLException;

    /** Get all unpaid invoices for a specific student. */
    List<Invoice> getUnpaidInvoicesByStudent(String studentId) throws SQLException;

    /** Mark an invoice as paid. Sets status to PAID and records paid_time. */
    Invoice payInvoice(int invoiceId) throws SQLException;
}
