package com.dorm.logic.code;

import com.dorm.model.Account;
import com.dorm.model.enums.Role;

// One signed-in user for this desktop application.
public class Session {
    private static Account account;

    public static Account getAccount() { return account; }
    public static void setAccount(Account value) { account = value; }
    public static void logout() { account = null; }
    public static void requireLogin() {
        if (account == null) throw new IllegalArgumentException("Please log in first.");
    }
    public static void requireManager() {
        requireLogin();
        if (account.getRole() == Role.STUDENT)
            throw new IllegalArgumentException("Only staff or admin can do this.");
    }
    public static void requireAdmin() {
        requireLogin();
        if (account.getRole() != Role.ADMIN)
            throw new IllegalArgumentException("Only admin can do this.");
    }
}
