package com.vaishnavi.sms.model;

/** Number of classes attended by one student, persisted in attendance.json. */
public class Attendance {

    private String studentId;
    private int classesAttended;

    public Attendance() {
    }

    public Attendance(String studentId, int classesAttended) {
        this.studentId = studentId;
        this.classesAttended = classesAttended;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public int getClassesAttended() { return classesAttended; }
    public void setClassesAttended(int classesAttended) { this.classesAttended = classesAttended; }
}
