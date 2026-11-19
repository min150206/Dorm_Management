package com.dorm.dao;

import com.dorm.model.Account;
import com.dorm.model.enums.Role;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AccountDAO extends GenericDAO<Account, Integer> {

    Optional<Account> findByEmail(String email) throws SQLException;

    List<Account> findByRole(Role role) throws SQLException;

    Optional<Account> findByEmailAndPassword(String email, String password) throws SQLException;

    boolean emailExists(String email) throws SQLException;
}
