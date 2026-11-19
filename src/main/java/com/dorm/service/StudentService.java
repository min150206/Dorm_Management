package com.dorm.service;

import com.dorm.model.Student;
import com.dorm.model.enums.Gender;
import java.util.List;
import java.util.Optional;

public interface StudentService {

    Optional<Student> getStudentById(String studentId);

    List<Student> getAllStudents();

    List<Student> searchStudentsByName(String keyword);

    List<Student> getStudentsByGender(Gender gender);

    Student updateStudent(Student student);

    boolean deleteStudent(String studentId);
}
