package com.dorm.logic.code;

import com.dorm.dao.impl.*;
import com.dorm.model.*;
import com.dorm.model.enums.*;
import com.dorm.logic.itf.*;
import java.sql.SQLException;
import java.util.*;

public class AuthServiceImpl implements AuthService {
    private final AccountDAOImpl accounts = new AccountDAOImpl();
    private final StudentDAOImpl students = new StudentDAOImpl();

    private String email(String email) {
        String value = ServiceSupport.text(email, "Email", 255).toLowerCase(Locale.ROOT);
        if (!value.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            throw new IllegalArgumentException("Invalid email address.");
        return value;
    }
    public Optional<Account> login(String email, String password) throws SQLException {
        Session.logout();
        Optional<Account> a = accounts.findByEmail(email(email));
        if (a.isPresent() && PasswordUtil.matches(password, a.get().getPassword())) {
            Session.setAccount(a.get());
            return a;
        }
        return Optional.empty();
    }
    public Account registerStudent(String email, String password, String id, String name,
                                   String phone, String gender) throws SQLException {
        String mail = email(email);
        String studentId = ServiceSupport.text(id, "Student ID", 10);
        String fullName = ServiceSupport.text(name, "Full name", 255);
        ServiceSupport.phone(phone);
        Gender sex = Gender.fromString(ServiceSupport.text(gender, "Gender", 10));
        String hash = PasswordUtil.hash(password);
        return ServiceSupport.transaction(() -> {
            if (accounts.count() == 0) throw new IllegalArgumentException("Create the first admin account before registering students.");
            if (accounts.emailExists(mail) || students.studentIdExists(studentId))
                throw new IllegalArgumentException("Email or student ID already exists.");
            Account a = accounts.save(new Account(null, mail, hash, Role.STUDENT));
            students.save(new Student(studentId, a.getId(), fullName, phone, sex));
            return a;
        });
    }
    public Account registerAdmin(String email, String password) throws SQLException {
        String mail = email(email);
        String hash = PasswordUtil.hash(password);
        return ServiceSupport.transaction(() -> {
            // Serialize first-admin setup across separate application instances as well.
            try (java.sql.Statement s = com.dorm.db.DatabaseConnection.getInstance().getConnection().createStatement()) {
                s.execute("LOCK TABLE account IN EXCLUSIVE MODE");
            }
            if (accounts.count() > 0) Session.requireAdmin();
            return accounts.save(new Account(null, mail, hash, Role.ADMIN));
        });
    }
    public Account registerStaff(String email, String password) throws SQLException {
        Session.requireAdmin();
        return accounts.save(new Account(null, email(email), PasswordUtil.hash(password), Role.STAFF));
    }
    public boolean changePassword(int id, String oldPassword, String newPassword) throws SQLException {
        Session.requireLogin();
        if (id != Session.getAccount().getId()) throw new IllegalArgumentException("Use your own account.");
        Account a = accounts.findById(id).orElseThrow(() -> new IllegalArgumentException("Account missing."));
        if (!PasswordUtil.matches(oldPassword, a.getPassword())) return false;
        a.setPassword(PasswordUtil.hash(newPassword));
        accounts.update(a);
        return true;
    }
    public boolean isEmailTaken(String email) throws SQLException { return accounts.emailExists(email(email)); }
    public Optional<Student> getStudentProfile(int id) throws SQLException {
        Session.requireLogin();
        if (id != Session.getAccount().getId()) Session.requireManager();
        return students.findByAccountId(id);
    }
}
