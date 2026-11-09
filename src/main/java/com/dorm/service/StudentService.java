package com.dorm.service;

import com.dorm.model.Student;
import com.dorm.model.enums.Gender;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for student management.
 */
public interface StudentService {

    Optional<Student> getStudentById(String studentId) throws SQLException;

    List<Student> getAllStudents() throws SQLException;

    List<Student> searchStudentsByName(String keyword) throws SQLException;

    List<Student> getStudentsByGender(Gender gender) throws SQLException;

    Student updateStudent(Student student) throws SQLException;

    boolean deleteStudent(String studentId) throws SQLException;
}
