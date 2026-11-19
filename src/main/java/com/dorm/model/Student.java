package com.dorm.model;

import com.dorm.model.enums.Gender;

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
