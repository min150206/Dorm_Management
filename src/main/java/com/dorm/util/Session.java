package com.dorm.util;

import com.dorm.exception.ServiceException;
import com.dorm.model.Account;
import com.dorm.model.enums.Role;

// Holds the currently logged-in account in the desktop session
public class Session {
    private static Account account;

    public static Account getAccount() {
        return account;
    }

    public static void setAccount(Account value) {
        account = value;
    }

    public static void logout() {
        account = null;
    }

    public static void requireLogin() {
        if (account == null) {
            throw new ServiceException("Please log in first.");
        }
    }

    public static void requireManager() {
        requireLogin();
        if (account.getRole() == Role.STUDENT) {
            throw new ServiceException("Permission denied. Only staff or admin can perform this action.");
        }
    }

    public static void requireAdmin() {
        requireLogin();
        if (account.getRole() != Role.ADMIN) {
            throw new ServiceException("Permission denied. Only admin can perform this action.");
        }
    }
}
