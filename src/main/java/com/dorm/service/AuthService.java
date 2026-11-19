package com.dorm.service;

import com.dorm.model.Account;
import com.dorm.model.Student;
import java.util.Optional;

public interface AuthService {

    Optional<Account> login(String email, String password);

    Account registerStudent(String email, String password,
                            String studentId, String fullName,
                            String phone, String gender);

    Account registerAdmin(String email, String password);

    Account registerStaff(String email, String password);

    boolean changePassword(int accountId, String oldPassword, String newPassword);

    boolean isEmailTaken(String email);

    Optional<Student> getStudentProfile(int accountId);
}
