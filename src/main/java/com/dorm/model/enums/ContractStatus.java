package com.dorm.model.enums;

public enum ContractStatus {
    ACTIVE,
    EXPIRED,
    TERMINATED;

    public static ContractStatus fromString(String value) {
        if (value == null) return null;
        try {
            return ContractStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown contract status: " + value);
        }
    }
}
