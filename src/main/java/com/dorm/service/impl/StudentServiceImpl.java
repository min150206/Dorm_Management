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
import com.dorm.service.StudentService;
import com.dorm.util.Session;
import com.dorm.util.ValidationUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class StudentServiceImpl implements StudentService {

    private final StudentDAO studentDao;
    private final AccountDAO accountDao;

    public StudentServiceImpl() {
        this(new StudentDAOImpl(), new AccountDAOImpl());
    }

    public StudentServiceImpl(StudentDAO studentDao, AccountDAO accountDao) {
        this.studentDao = studentDao;
        this.accountDao = accountDao;
    }

    @Override
    public Optional<Student> getStudentById(String id) {
        ValidationUtils.checkStudentOwnership(id, studentDao);
        try {
            return studentDao.findById(id);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch student: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Student> getAllStudents() {
        Session.requireManager();
        try {
            return studentDao.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch students: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Student> searchStudentsByName(String name) {
        Session.requireManager();
        try {
            return studentDao.searchByName(name == null ? "" : name);
        } catch (SQLException e) {
            throw new ServiceException("Failed to search students: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Student> getStudentsByGender(Gender gender) {
        Session.requireManager();
        Objects.requireNonNull(gender, "Gender is required");
        try {
            return studentDao.findByGender(gender);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch students by gender: " + e.getMessage(), e);
        }
    }

    @Override
    public Student updateStudent(Student student) {
        ValidationUtils.checkStudentOwnership(student.getId(), studentDao);
        String name = ValidationUtils.requireText(student.getFullName(), "Full name", 255);
        ValidationUtils.requirePhone(student.getPhone());
        if (student.getGender() == null) {
            throw new ServiceException("Gender is required.");
        }

        try {
            Student existing = studentDao.findById(student.getId())
                    .orElseThrow(() -> new ServiceException("Student not found."));
            student.setAccountId(existing.getAccountId());
            student.setFullName(name);
            return studentDao.update(student);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update student: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteStudent(String studentId) {
        Session.requireManager();
        return TransactionManager.execute(() -> {
            Optional<Student> student = studentDao.findById(studentId);
            if (student.isEmpty()) {
                return false;
            }

            boolean deleted = studentDao.deleteById(studentId);
            Integer accountId = student.get().getAccountId();
            if (deleted && accountId != null) {
                Optional<Account> account = accountDao.findById(accountId);
                if (account.isPresent() && account.get().getRole() == Role.STUDENT) {
                    accountDao.deleteById(accountId);
                }
            }
            return deleted;
        });
    }
}
