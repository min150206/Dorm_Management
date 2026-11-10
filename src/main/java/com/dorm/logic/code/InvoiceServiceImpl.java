package com.dorm.logic.code;

import com.dorm.dao.impl.*;
import com.dorm.model.*;
import com.dorm.model.enums.InvoiceStatus;
import com.dorm.logic.itf.*;
import java.sql.*;
import java.util.*;

public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceDAOImpl invoices = new InvoiceDAOImpl();
    public Invoice createInvoice(int contractId, String title, long fee) throws SQLException {
        Session.requireManager();
        if (!new ContractDAOImpl().existsById(contractId)) throw new IllegalArgumentException("Contract missing.");
        if (fee < 0) throw new IllegalArgumentException("Fee cannot be negative.");
        return invoices.save(new Invoice(null, contractId, ServiceSupport.text(title, "Title", 255), fee, InvoiceStatus.UNPAID, null));
    }
    private void checkOwner(Invoice i) throws SQLException {
        Contract c = new ContractDAOImpl().findById(i.getContractId()).orElseThrow();
        ServiceSupport.ownStudent(c.getStudentId());
    }
    public Optional<Invoice> getInvoiceById(int id) throws SQLException {
        Session.requireLogin(); Optional<Invoice> i = invoices.findById(id);
        if (i.isPresent()) checkOwner(i.get()); return i;
    }
    public List<Invoice> getInvoicesByContract(int id) throws SQLException {
        Session.requireLogin(); Contract c = new ContractDAOImpl().findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contract missing."));
        ServiceSupport.ownStudent(c.getStudentId()); return invoices.findByContractId(id);
    }
    public List<Invoice> getUnpaidInvoices() throws SQLException { Session.requireManager(); return invoices.findUnpaidInvoices(); }
    public List<Invoice> getUnpaidInvoicesByStudent(String id) throws SQLException { ServiceSupport.ownStudent(id); return invoices.findUnpaidByStudentId(id); }
    public List<Invoice> getAllInvoices() throws SQLException { Session.requireManager(); return invoices.findAll(); }
    public Invoice payInvoice(int id) throws SQLException {
        // This is a staff record of money received, not an online payment.
        Session.requireManager();
        return ServiceSupport.transaction(() -> {
            ServiceSupport.lock("SELECT invoice_id FROM invoice WHERE invoice_id=? FOR UPDATE", id);
            Invoice i = invoices.findById(id).orElseThrow();
            if (i.getInvoiceStatus() == InvoiceStatus.PAID) return i;
            i.setInvoiceStatus(InvoiceStatus.PAID); i.setPaidTime(new Timestamp(System.currentTimeMillis()));
            return invoices.update(i);
        });
    }
}
