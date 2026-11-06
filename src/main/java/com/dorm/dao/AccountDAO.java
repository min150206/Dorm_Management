package com.dorm.dao;

import com.dorm.model.Account;
import com.dorm.model.enums.Role;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for the 'account' table.
 * Extends GenericDAO with account-specific query methods.
 */
public interface AccountDAO extends GenericDAO<Account, Integer> {

    /** Find an account by its unique email address. */
    Optional<Account> findByEmail(String email) throws SQLException;

    /** Find all accounts with the given role. */
    List<Account> findByRole(Role role) throws SQLException;

    /** Authenticate: find an account matching both email and password. */
    Optional<Account> findByEmailAndPassword(String email, String password) throws SQLException;

    /** Check whether an email already exists (for registration validation). */
    boolean emailExists(String email) throws SQLException;
}
