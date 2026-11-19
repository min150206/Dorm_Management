package com.dorm.service.impl;

import com.dorm.dao.ContractDAO;
import com.dorm.dao.InvoiceDAO;
import com.dorm.dao.StudentDAO;
import com.dorm.dao.impl.ContractDAOImpl;
import com.dorm.dao.impl.InvoiceDAOImpl;
import com.dorm.dao.impl.StudentDAOImpl;
import com.dorm.db.TransactionManager;
import com.dorm.exception.ServiceException;
import com.dorm.model.Contract;
import com.dorm.model.Invoice;
import com.dorm.model.enums.InvoiceStatus;
import com.dorm.service.InvoiceService;
import com.dorm.util.Session;
import com.dorm.util.ValidationUtils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceDAO invoiceDao;
    private final ContractDAO contractDao;
    private final StudentDAO studentDao;

    public InvoiceServiceImpl() {
        this(new InvoiceDAOImpl(), new ContractDAOImpl(), new StudentDAOImpl());
    }

    public InvoiceServiceImpl(InvoiceDAO invoiceDao, ContractDAO contractDao, StudentDAO studentDao) {
        this.invoiceDao = invoiceDao;
        this.contractDao = contractDao;
        this.studentDao = studentDao;
    }

    @Override
    public Invoice createInvoice(int contractId, String title, long fee) {
        Session.requireManager();
        if (fee < 0) {
            throw new ServiceException("Invoice fee cannot be negative.");
        }
        String validTitle = ValidationUtils.requireText(title, "Title", 255);

        try {
            if (!contractDao.existsById(contractId)) {
                throw new ServiceException("Contract not found.");
            }
            return invoiceDao.save(new Invoice(null, contractId, validTitle, fee, InvoiceStatus.UNPAID, null));
        } catch (SQLException e) {
            throw new ServiceException("Failed to create invoice: " + e.getMessage(), e);
        }
    }

    private void checkInvoiceOwnership(Invoice invoice) {
        try {
            Contract contract = contractDao.findById(invoice.getContractId())
                    .orElseThrow(() -> new ServiceException("Contract not found."));
            ValidationUtils.checkStudentOwnership(contract.getStudentId(), studentDao);
        } catch (SQLException e) {
            throw new ServiceException("Failed to verify invoice ownership: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Invoice> getInvoiceById(int id) {
        Session.requireLogin();
        try {
            Optional<Invoice> invoice = invoiceDao.findById(id);
            invoice.ifPresent(this::checkInvoiceOwnership);
            return invoice;
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch invoice: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Invoice> getInvoicesByContract(int contractId) {
        Session.requireLogin();
        try {
            Contract contract = contractDao.findById(contractId)
                    .orElseThrow(() -> new ServiceException("Contract not found."));
            ValidationUtils.checkStudentOwnership(contract.getStudentId(), studentDao);
            return invoiceDao.findByContractId(contractId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch contract invoices: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Invoice> getUnpaidInvoices() {
        Session.requireManager();
        try {
            return invoiceDao.findUnpaidInvoices();
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch unpaid invoices: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Invoice> getUnpaidInvoicesByStudent(String studentId) {
        ValidationUtils.checkStudentOwnership(studentId, studentDao);
        try {
            return invoiceDao.findUnpaidByStudentId(studentId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch student invoices: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Invoice> getAllInvoices() {
        Session.requireManager();
        try {
            return invoiceDao.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch invoices: " + e.getMessage(), e);
        }
    }

    @Override
    public Invoice payInvoice(int id) {
        Session.requireManager();
        return TransactionManager.execute(() -> {
            Invoice invoice = invoiceDao.findById(id)
                    .orElseThrow(() -> new ServiceException("Invoice not found."));
            if (invoice.getInvoiceStatus() == InvoiceStatus.PAID) {
                return invoice;
            }
            invoice.setInvoiceStatus(InvoiceStatus.PAID);
            invoice.setPaidTime(new Timestamp(System.currentTimeMillis()));
            return invoiceDao.update(invoice);
        });
    }
}
