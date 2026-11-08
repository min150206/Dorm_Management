package com.dorm.dao;

import com.dorm.model.Student;
import com.dorm.model.enums.Gender;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * DAO interface for the 'student' table.
 * PK type is String (student_id is VARCHAR).
 */
public interface StudentDAO extends GenericDAO<Student, String> {

    /** Find the student linked to a given account. */
    Optional<Student> findByAccountId(int accountId) throws SQLException;

    /** Search students whose full_name contains the keyword (case-insensitive). */
    List<Student> searchByName(String keyword) throws SQLException;

    /** Find all students of a specific gender. */
    List<Student> findByGender(Gender gender) throws SQLException;

    /** Check if a student_id already exists. */
    boolean studentIdExists(String studentId) throws SQLException;
}
