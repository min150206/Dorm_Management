package com.dorm.model;

import com.dorm.model.enums.Gender;

/**
 * Entity representing the 'student' table.
 * PK is student_id (VARCHAR), so this entity uses String as its ID type.
 *
 * Relationships:
 *  - N-to-1 with Account  (has profile, via account_id FK)
 *  - 1-to-N with Request  (submits)
 *  - 1-to-N with Contract (signs)
 */
public class Student extends BaseEntity<String> {

    private Integer accountId;
    private String fullName;
    private String phone;
    private Gender gender;

    public Student() {
    }

    public Student(String studentId, Integer accountId, String fullName, String phone, Gender gender) {
        super(studentId);
        this.accountId = accountId;
        this.fullName  = fullName;
        this.phone     = phone;
        this.gender    = gender;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public String getStudentId()                  { return getId(); }
    public void   setStudentId(String id)         { setId(id); }

    public Integer getAccountId()                 { return accountId; }
    public void    setAccountId(Integer accountId) { this.accountId = accountId; }

    public String getFullName()                   { return fullName; }
    public void   setFullName(String fullName)    { this.fullName = fullName; }

    public String getPhone()                      { return phone; }
    public void   setPhone(String phone)          { this.phone = phone; }

    public Gender getGender()                     { return gender; }
    public void   setGender(Gender gender)        { this.gender = gender; }

    // ── BaseEntity contract ───────────────────────────────────────────

    @Override
    public String getTableName()    { return "student"; }

    @Override
    public String getIdColumnName() { return "student_id"; }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + getId() + '\'' +
                ", accountId=" + accountId +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", gender=" + gender +
                '}';
    }
}
