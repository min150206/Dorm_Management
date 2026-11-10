package com.dorm.logic.code;

import com.dorm.dao.impl.StudentDAOImpl;
import com.dorm.model.Student;
import com.dorm.model.enums.Gender;
import com.dorm.logic.itf.*;
import java.sql.SQLException;
import java.util.*;

public class StudentServiceImpl implements StudentService {
    private final StudentDAOImpl dao = new StudentDAOImpl();
    public Optional<Student> getStudentById(String id) throws SQLException {
        ServiceSupport.ownStudent(id); return dao.findById(id);
    }
    public List<Student> getAllStudents() throws SQLException { Session.requireManager(); return dao.findAll(); }
    public List<Student> searchStudentsByName(String name) throws SQLException {
        Session.requireManager(); return dao.searchByName(name == null ? "" : name);
    }
    public List<Student> getStudentsByGender(Gender gender) throws SQLException {
        Session.requireManager(); Objects.requireNonNull(gender, "Gender is required"); return dao.findByGender(gender);
    }
    public Student updateStudent(Student s) throws SQLException {
        ServiceSupport.ownStudent(s.getId());
        Student old = dao.findById(s.getId()).orElseThrow(() -> new IllegalArgumentException("Student missing."));
        s.setAccountId(old.getAccountId());
        s.setFullName(ServiceSupport.text(s.getFullName(), "Full name", 255));
        ServiceSupport.phone(s.getPhone());
        if (s.getGender() == null) throw new IllegalArgumentException("Choose gender.");
        return dao.update(s);
    }
    public boolean deleteStudent(String id) throws SQLException {
        Session.requireManager();
        return ServiceSupport.transaction(() -> {
            Optional<Student> old = dao.findById(id);
            if (old.isEmpty()) return false;
            boolean deleted = dao.deleteById(id);
            // Remove the student's login too, so it cannot remain without a profile.
            Integer accountId = old.get().getAccountId();
            if (deleted && accountId != null) {
                com.dorm.dao.impl.AccountDAOImpl accounts = new com.dorm.dao.impl.AccountDAOImpl();
                com.dorm.model.Account account = accounts.findById(accountId).orElseThrow();
                if (account.getRole() == com.dorm.model.enums.Role.STUDENT) accounts.deleteById(accountId);
            }
            return deleted;
        });
    }
}
