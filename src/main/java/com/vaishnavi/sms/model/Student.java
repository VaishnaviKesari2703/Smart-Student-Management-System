package com.vaishnavi.sms.model;

/** Student profile record persisted in students.json. */
public class Student {

    private String id;
    private String registrationNumber;
    private String fullName;
    private String email;
    private String department;
    private int year;
    private int semester;
    private String username;

    public Student() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
