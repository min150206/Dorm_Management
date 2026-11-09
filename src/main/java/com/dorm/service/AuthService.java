package com.dorm.service;

import com.dorm.model.Account;
import com.dorm.model.Student;
import com.dorm.model.enums.Role;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Service interface for authentication and account management.
 */
public interface AuthService {

    /** Authenticate a user by email and password. Returns the account if credentials match. */
    Optional<Account> login(String email, String password) throws SQLException;

    /** Register a new student account. Creates both Account and Student records. */
    Account registerStudent(String email, String password,
                            String studentId, String fullName,
                            String phone, String gender) throws SQLException;

    /** Register a new admin account. */
    Account registerAdmin(String email, String password) throws SQLException;

    /** Change the password for an account. Returns true if successful. */
    boolean changePassword(int accountId, String oldPassword, String newPassword) throws SQLException;

    /** Check whether an email is already taken. */
    boolean isEmailTaken(String email) throws SQLException;

    /** Find the student profile linked to an account (if any). */
    Optional<Student> getStudentProfile(int accountId) throws SQLException;
}
