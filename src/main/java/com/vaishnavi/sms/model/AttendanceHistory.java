package com.vaishnavi.sms.model;

import java.time.LocalDateTime;

/** One simulated attendance adjustment, persisted in attendance_history.json. */
public class AttendanceHistory {

    private String id;
    private String studentId;
    private int previousAttendance;
    private int newAttendance;
    /** INCREMENT, DECREMENT or ADMIN_EDIT. */
    private String action;
    private LocalDateTime timestamp;

    public AttendanceHistory() {
    }

    public AttendanceHistory(String id, String studentId, int previousAttendance, int newAttendance,
                             String action, LocalDateTime timestamp) {
        this.id = id;
        this.studentId = studentId;
        this.previousAttendance = previousAttendance;
        this.newAttendance = newAttendance;
        this.action = action;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public int getPreviousAttendance() { return previousAttendance; }
    public void setPreviousAttendance(int previousAttendance) { this.previousAttendance = previousAttendance; }
    public int getNewAttendance() { return newAttendance; }
    public void setNewAttendance(int newAttendance) { this.newAttendance = newAttendance; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
