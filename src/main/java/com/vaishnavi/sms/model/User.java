package com.vaishnavi.sms.model;

/** Login account persisted in users.json. The password is stored only as a BCrypt hash. */
public class User {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_STUDENT = "STUDENT";

    private String id;
    private String username;
    private String passwordHash;
    private String role;
    /** Linked student id; null for the admin account. */
    private String studentId;

    public User() {
    }

    public User(String id, String username, String passwordHash, String role, String studentId) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.studentId = studentId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
}
