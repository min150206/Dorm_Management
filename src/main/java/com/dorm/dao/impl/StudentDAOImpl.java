package com.dorm.dao.impl;

import com.dorm.dao.StudentDAO;
import com.dorm.model.Student;
import com.dorm.model.enums.Gender;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Concrete DAO implementation for the 'student' table.
 * Note: student_id is VARCHAR (String PK), not SERIAL.
 */
public class StudentDAOImpl extends AbstractDAO<Student, String> implements StudentDAO {

    // ── AbstractDAO template methods ──────────────────────────────────

    @Override
    protected String getTableName() {
        return "student";
    }

    @Override
    protected String getIdColumnName() {
        return "student_id";
    }

    @Override
    protected Student mapRow(ResultSet rs) throws SQLException {
        String genderStr = rs.getString("gender");
        return new Student(
                rs.getString("student_id"),
                rs.getInt("account_id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                genderStr != null ? Gender.fromString(genderStr) : null
        );
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO student (student_id, account_id, full_name, phone, gender) "
             + "VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE student SET account_id = ?, full_name = ?, phone = ?, gender = ? "
             + "WHERE student_id = ?";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Student entity) throws SQLException {
        ps.setString(1, entity.getStudentId());
        ps.setInt(2, entity.getAccountId());
        ps.setString(3, entity.getFullName());
        ps.setString(4, entity.getPhone());
        ps.setString(5, entity.getGender() != null ? entity.getGender().name() : null);
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Student entity) throws SQLException {
        ps.setInt(1, entity.getAccountId());
        ps.setString(2, entity.getFullName());
        ps.setString(3, entity.getPhone());
        ps.setString(4, entity.getGender() != null ? entity.getGender().name() : null);
        ps.setString(5, entity.getStudentId());
    }

    /**
     * Override save() because student_id is not auto-generated (VARCHAR PK).
     * We skip reading generated keys since the PK is provided by the caller.
     */
    @Override
    public Student save(Student entity) throws SQLException {
        String sql = getInsertSQL();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            setInsertParameters(ps, entity);
            ps.executeUpdate();
        }
        return entity;
    }

    // ── StudentDAO-specific methods ───────────────────────────────────

    @Override
    public Optional<Student> findByAccountId(int accountId) throws SQLException {
        return querySingle("SELECT * FROM student WHERE account_id = ?", accountId);
    }

    @Override
    public List<Student> searchByName(String keyword) throws SQLException {
        String pattern = "%" + keyword.toLowerCase() + "%";
        return queryList("SELECT * FROM student WHERE LOWER(full_name) LIKE ?", pattern);
    }

    @Override
    public List<Student> findByGender(Gender gender) throws SQLException {
        return queryList("SELECT * FROM student WHERE gender = ?", gender.name());
    }

    @Override
    public boolean studentIdExists(String studentId) throws SQLException {
        return existsById(studentId);
    }
}
