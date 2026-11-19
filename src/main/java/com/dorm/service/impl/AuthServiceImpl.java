package com.dorm.service.impl;

import com.dorm.dao.AccountDAO;
import com.dorm.dao.StudentDAO;
import com.dorm.dao.impl.AccountDAOImpl;
import com.dorm.dao.impl.StudentDAOImpl;
import com.dorm.db.TransactionManager;
import com.dorm.exception.ServiceException;
import com.dorm.model.Account;
import com.dorm.model.Student;
import com.dorm.model.enums.Gender;
import com.dorm.model.enums.Role;
import com.dorm.service.AuthService;
import com.dorm.util.PasswordUtil;
import com.dorm.util.Session;
import com.dorm.util.ValidationUtils;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

public class AuthServiceImpl implements AuthService {

    private final AccountDAO accountDao;
    private final StudentDAO studentDao;

    public AuthServiceImpl() {
        this(new AccountDAOImpl(), new StudentDAOImpl());
    }

    public AuthServiceImpl(AccountDAO accountDao, StudentDAO studentDao) {
        this.accountDao = accountDao;
        this.studentDao = studentDao;
    }

    private String validateEmail(String email) {
        String value = ValidationUtils.requireText(email, "Email", 255).toLowerCase(Locale.ROOT);
        if (!value.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new ServiceException("Invalid email format.");
        }
        return value;
    }

    @Override
    public Optional<Account> login(String email, String password) {
        Session.logout();
        try {
            Optional<Account> account = accountDao.findByEmail(validateEmail(email));
            if (account.isPresent() && PasswordUtil.matches(password, account.get().getPassword())) {
                Session.setAccount(account.get());
                return account;
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new ServiceException("Login query failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Account registerStudent(String email, String password, String studentId,
                                   String fullName, String phone, String gender) {
        String mail = validateEmail(email);
        String sId = ValidationUtils.requireText(studentId, "Student ID", 10);
        String name = ValidationUtils.requireText(fullName, "Full name", 255);
        ValidationUtils.requirePhone(phone);
        Gender sex = Gender.fromString(ValidationUtils.requireText(gender, "Gender", 10));
        String hash = PasswordUtil.hash(password);

        return TransactionManager.execute(() -> {
            if (accountDao.count() == 0) {
                throw new ServiceException("An admin account must be created before registering students.");
            }
            if (accountDao.emailExists(mail) || studentDao.studentIdExists(sId)) {
                throw new ServiceException("Email or Student ID already exists.");
            }
            Account account = accountDao.save(new Account(null, mail, hash, Role.STUDENT));
            studentDao.save(new Student(sId, account.getId(), name, phone, sex));
            return account;
        });
    }

    @Override
    public Account registerAdmin(String email, String password) {
        String mail = validateEmail(email);
        String hash = PasswordUtil.hash(password);

        return TransactionManager.execute(() -> {
            if (accountDao.count() > 0) {
                Session.requireAdmin();
            }
            if (accountDao.emailExists(mail)) {
                throw new ServiceException("Email is already taken.");
            }
            return accountDao.save(new Account(null, mail, hash, Role.ADMIN));
        });
    }

    @Override
    public Account registerStaff(String email, String password) {
        Session.requireAdmin();
        String mail = validateEmail(email);
        String hash = PasswordUtil.hash(password);

        try {
            if (accountDao.emailExists(mail)) {
                throw new ServiceException("Email is already taken.");
            }
            return accountDao.save(new Account(null, mail, hash, Role.STAFF));
        } catch (SQLException e) {
            throw new ServiceException("Failed to register staff: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean changePassword(int accountId, String oldPassword, String newPassword) {
        Session.requireLogin();
        if (accountId != Session.getAccount().getId()) {
            throw new ServiceException("You can only change password for your own account.");
        }
        try {
            Account account = accountDao.findById(accountId)
                    .orElseThrow(() -> new ServiceException("Account not found."));
            if (!PasswordUtil.matches(oldPassword, account.getPassword())) {
                return false;
            }
            account.setPassword(PasswordUtil.hash(newPassword));
            accountDao.update(account);
            return true;
        } catch (SQLException e) {
            throw new ServiceException("Failed to update password: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isEmailTaken(String email) {
        try {
            return accountDao.emailExists(validateEmail(email));
        } catch (SQLException e) {
            throw new ServiceException("Failed to check email: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Student> getStudentProfile(int accountId) {
        Session.requireLogin();
        if (accountId != Session.getAccount().getId()) {
            Session.requireManager();
        }
        try {
            return studentDao.findByAccountId(accountId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch student profile: " + e.getMessage(), e);
        }
    }
}
