package com.dorm.util;

import com.dorm.dao.StudentDAO;
import com.dorm.exception.ServiceException;
import com.dorm.model.enums.Role;
import java.sql.SQLException;

public class ValidationUtils {

    public static String requireText(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty() || value.trim().length() > maxLength) {
            throw new ServiceException(fieldName + " is required (max " + maxLength + " characters).");
        }
        return value.trim();
    }

    public static void requirePhone(String value) {
        if (value == null || !value.matches("0[0-9]{9}")) {
            throw new ServiceException("Phone number must contain exactly 10 digits starting with 0.");
        }
    }

    public static void checkStudentOwnership(String studentId, StudentDAO studentDao) {
        Session.requireLogin();
        if (Session.getAccount().getRole() == Role.STUDENT) {
            try {
                String ownStudentId = studentDao.findByAccountId(Session.getAccount().getId())
                        .orElseThrow(() -> new ServiceException("Student profile not found."))
                        .getId();
                if (!ownStudentId.equals(studentId)) {
                    throw new ServiceException("Access denied: You can only view your own records.");
                }
            } catch (SQLException e) {
                throw new ServiceException("Error checking student profile: " + e.getMessage(), e);
            }
        }
    }
}
