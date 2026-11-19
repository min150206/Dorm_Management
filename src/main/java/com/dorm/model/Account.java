package com.dorm.model;

import com.dorm.model.enums.Role;

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

    public Integer getAccountId()               { return getId(); }
    public void    setAccountId(Integer id)      { setId(id); }

    public String  getEmail()                    { return email; }
    public void    setEmail(String email)        { this.email = email; }

    public String  getPassword()                 { return password; }
    public void    setPassword(String password)  { this.password = password; }

    public Role    getRole()                     { return role; }
    public void    setRole(Role role)            { this.role = role; }

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + getId() +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
