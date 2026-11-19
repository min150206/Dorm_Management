package com.dorm.service;

import com.dorm.model.Invoice;
import java.util.List;
import java.util.Optional;

public interface InvoiceService {

    Invoice createInvoice(int contractId, String title, long fee);

    Optional<Invoice> getInvoiceById(int invoiceId);

    List<Invoice> getInvoicesByContract(int contractId);

    List<Invoice> getUnpaidInvoices();

    List<Invoice> getUnpaidInvoicesByStudent(String studentId);

    List<Invoice> getAllInvoices();

    Invoice payInvoice(int invoiceId);
}
