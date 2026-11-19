package com.dorm.dao;

import com.dorm.model.Student;
import com.dorm.model.enums.Gender;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface StudentDAO extends GenericDAO<Student, String> {

    Optional<Student> findByAccountId(int accountId) throws SQLException;

    List<Student> searchByName(String keyword) throws SQLException;

    List<Student> findByGender(Gender gender) throws SQLException;

    boolean studentIdExists(String studentId) throws SQLException;
}
