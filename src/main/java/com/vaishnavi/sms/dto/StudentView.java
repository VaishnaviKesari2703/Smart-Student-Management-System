package com.vaishnavi.sms.dto;

import com.vaishnavi.sms.model.Student;

/** Student record combined with calculated attendance, returned by the REST API. */
public class StudentView {

    private final String id;
    private final String registrationNumber;
    private final String fullName;
    private final String email;
    private final String department;
    private final int year;
    private final int semester;
    private final String username;
    private final AttendanceSummary attendance;

    public StudentView(Student s, AttendanceSummary attendance) {
        this.id = s.getId();
        this.registrationNumber = s.getRegistrationNumber();
        this.fullName = s.getFullName();
        this.email = s.getEmail();
        this.department = s.getDepartment();
        this.year = s.getYear();
        this.semester = s.getSemester();
        this.username = s.getUsername();
        this.attendance = attendance;
    }

    public String getId() { return id; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public int getYear() { return year; }
    public int getSemester() { return semester; }
    public String getUsername() { return username; }
    public AttendanceSummary getAttendance() { return attendance; }
}
