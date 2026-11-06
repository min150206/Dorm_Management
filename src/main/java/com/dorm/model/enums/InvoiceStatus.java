package com.dorm.model.enums;

public enum InvoiceStatus {
    UNPAID,
    PAID;

    public static InvoiceStatus fromString(String value) {
        if (value == null) return null;
        try {
            return InvoiceStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown invoice status: " + value);
        }
    }
}
