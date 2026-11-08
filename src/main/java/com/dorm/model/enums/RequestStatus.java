package com.dorm.model.enums;

public enum RequestStatus {
    PENDING,
    PROCESSING,
    RESOLVED;

    public static RequestStatus fromString(String value) {
        if (value == null) return null;
        try {
            return RequestStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown request status: " + value);
        }
    }
}
