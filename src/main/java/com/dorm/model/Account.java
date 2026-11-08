package com.dorm.model;

import com.dorm.model.enums.Role;

/**
 * Entity representing the 'account' table.
 *
 * Relationships:
 *  - 1-to-1 with Student (has profile)
 *  - 1-to-N with Request  (resolves — via resolved_by)
 */
public class Account extends BaseEntity<Integer> {

    private String email;
    private String password;
    private Role   role;

    public Account() {
    }

    public Account(Integer accountId, String email, String password, Role role) {
        super(accountId);
        this.email    = email;
        this.password = password;
        this.role     = role;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Integer getAccountId()               { return getId(); }
    public void    setAccountId(Integer id)      { setId(id); }

    public String  getEmail()                    { return email; }
    public void    setEmail(String email)        { this.email = email; }

    public String  getPassword()                 { return password; }
    public void    setPassword(String password)  { this.password = password; }

    public Role    getRole()                     { return role; }
    public void    setRole(Role role)            { this.role = role; }

    // ── BaseEntity contract ───────────────────────────────────────────

    @Override
    public String getTableName()    { return "account"; }

    @Override
    public String getIdColumnName() { return "account_id"; }

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + getId() +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
