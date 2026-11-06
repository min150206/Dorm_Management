package com.dorm.dao.impl;

import com.dorm.dao.AccountDAO;
import com.dorm.model.Account;
import com.dorm.model.enums.Role;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Concrete DAO implementation for the 'account' table.
 */
public class AccountDAOImpl extends AbstractDAO<Account, Integer> implements AccountDAO {

    // ── AbstractDAO template methods ──────────────────────────────────

    @Override
    protected String getTableName() {
        return "account";
    }

    @Override
    protected String getIdColumnName() {
        return "account_id";
    }

    @Override
    protected Account mapRow(ResultSet rs) throws SQLException {
        return new Account(
                rs.getInt("account_id"),
                rs.getString("email"),
                rs.getString("password"),
                Role.fromString(rs.getString("role"))
        );
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO account (email, password, role) VALUES (?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE account SET email = ?, password = ?, role = ? WHERE account_id = ?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Account entity) throws SQLException {
        ps.setString(1, entity.getEmail());
        ps.setString(2, entity.getPassword());
        ps.setString(3, entity.getRole().name());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Account entity) throws SQLException {
        ps.setString(1, entity.getEmail());
        ps.setString(2, entity.getPassword());
        ps.setString(3, entity.getRole().name());
        ps.setInt(4, entity.getAccountId());
    }

    // ── AccountDAO-specific methods ───────────────────────────────────

    @Override
    public Optional<Account> findByEmail(String email) throws SQLException {
        return querySingle("SELECT * FROM account WHERE email = ?", email);
    }

    @Override
    public List<Account> findByRole(Role role) throws SQLException {
        return queryList("SELECT * FROM account WHERE role = ?", role.name());
    }

    @Override
    public Optional<Account> findByEmailAndPassword(String email, String password) throws SQLException {
        return querySingle("SELECT * FROM account WHERE email = ? AND password = ?", email, password);
    }

    @Override
    public boolean emailExists(String email) throws SQLException {
        return querySingle("SELECT * FROM account WHERE email = ?", email).isPresent();
    }
}
