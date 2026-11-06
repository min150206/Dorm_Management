package com.dorm.model;

import com.dorm.model.enums.InvoiceStatus;
import java.sql.Timestamp;

/**
 * Entity representing the 'invoice' table.
 *
 * Relationships:
 *  - N-to-1 with Contract (bills, via contract_id FK)
 */
public class Invoice extends BaseEntity<Integer> {

    private int           contractId;
    private String        title;
    private long          fee;
    private InvoiceStatus invoiceStatus;
    private Timestamp     paidTime;

    public Invoice() {
    }

    public Invoice(Integer invoiceId, int contractId, String title,
                   long fee, InvoiceStatus invoiceStatus, Timestamp paidTime) {
        super(invoiceId);
        this.contractId    = contractId;
        this.title         = title;
        this.fee           = fee;
        this.invoiceStatus = invoiceStatus;
        this.paidTime      = paidTime;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Integer       getInvoiceId()                                 { return getId(); }
    public void          setInvoiceId(Integer id)                        { setId(id); }

    public int           getContractId()                                { return contractId; }
    public void          setContractId(int contractId)                  { this.contractId = contractId; }

    public String        getTitle()                                     { return title; }
    public void          setTitle(String title)                         { this.title = title; }

    public long          getFee()                                       { return fee; }
    public void          setFee(long fee)                               { this.fee = fee; }

    public InvoiceStatus getInvoiceStatus()                             { return invoiceStatus; }
    public void          setInvoiceStatus(InvoiceStatus invoiceStatus)  { this.invoiceStatus = invoiceStatus; }

    public Timestamp     getPaidTime()                                  { return paidTime; }
    public void          setPaidTime(Timestamp paidTime)                { this.paidTime = paidTime; }

    // ── BaseEntity contract ───────────────────────────────────────────

    @Override
    public String getTableName()    { return "invoice"; }

    @Override
    public String getIdColumnName() { return "invoice_id"; }

    @Override
    public String toString() {
        return "Invoice{" +
                "invoiceId=" + getId() +
                ", contractId=" + contractId +
                ", title='" + title + '\'' +
                ", fee=" + fee +
                ", invoiceStatus=" + invoiceStatus +
                '}';
    }
}
