package com.dorm.model.enums;

public enum RoomStatus {
    AVAILABLE,
    FULL,
    MAINTENANCE;

    public static RoomStatus fromString(String value) {
        if (value == null) return null;
        try {
            return RoomStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown room status: " + value);
        }
    }
}
