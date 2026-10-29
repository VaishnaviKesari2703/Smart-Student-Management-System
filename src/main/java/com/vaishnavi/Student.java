package com.vaishnavi;

/**
 * Student model with validation and grade calculation.
 * Records are stored in-memory and are NOT persistent across restarts.
 */
public class Student {

    private String id;
    private String name;
    private String department;
    private double marks;

    public Student(String id, String name, String department, double marks) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.marks = marks;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getId()         { return id; }
    public String getName()       { return name; }
    public String getDepartment() { return department; }
    public double getMarks()      { return marks; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setName(String name)             { this.name = name; }
    public void setDepartment(String department) { this.department = department; }
    public void setMarks(double marks)           { this.marks = marks; }

    // ── Grade Calculation ─────────────────────────────────────────────────────

    /**
     * Calculates letter grade based on marks (0–100).
     */
    public String getGrade() {
        if (marks >= 90) return "A+";
        if (marks >= 80) return "A";
        if (marks >= 70) return "B";
        if (marks >= 60) return "C";
        if (marks >= 50) return "D";
        return "F";
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /**
     * Validates all student fields and throws IllegalArgumentException on failure.
     */
    public static void validate(String id, String name, String department, double marks) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        if (!id.trim().matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException(
                "Student ID must contain only letters, digits, hyphens, or underscores.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Student name cannot be empty.");
        }
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be empty.");
        }
        if (marks < 0 || marks > 100) {
            throw new IllegalArgumentException(
                "Marks must be between 0 and 100. Provided: " + marks);
        }
    }

    @Override
    public String toString() {
        return String.format(
            "| %-10s | %-20s | %-15s | %6.2f | %3s |",
            id, name, department, marks, getGrade());
    }
}
