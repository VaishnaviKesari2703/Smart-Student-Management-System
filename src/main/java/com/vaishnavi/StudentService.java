package com.vaishnavi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business logic layer: CRUD operations and search for students.
 * Uses a LinkedHashMap to preserve insertion order.
 * NOTE: All data is in-memory and lost when the application exits.
 */
public class StudentService {

    private final Map<String, Student> studentStore = new LinkedHashMap<>();

    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * Adds a new student after validating all fields and checking for duplicate ID.
     *
     * @throws IllegalArgumentException if validation fails or the ID already exists.
     */
    public void addStudent(String id, String name, String department, double marks) {
        String trimmedId = id == null ? "" : id.trim();
        Student.validate(trimmedId, name, department, marks);

        if (studentStore.containsKey(trimmedId)) {
            throw new IllegalArgumentException(
                "A student with ID '" + trimmedId + "' already exists.");
        }

        studentStore.put(trimmedId, new Student(trimmedId, name.trim(),
                                                 department.trim(), marks));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns an unmodifiable list of all students.
     */
    public List<Student> getAllStudents() {
        return new ArrayList<>(studentStore.values());
    }

    /**
     * Finds a student by exact ID (case-sensitive).
     *
     * @throws IllegalArgumentException if the student is not found.
     */
    public Student findById(String id) {
        Student student = studentStore.get(id == null ? "" : id.trim());
        if (student == null) {
            throw new IllegalArgumentException(
                "No student found with ID '" + id + "'.");
        }
        return student;
    }

    /**
     * Finds all students whose name contains the query string (case-insensitive).
     */
    public List<Student> findByName(String nameQuery) {
        if (nameQuery == null || nameQuery.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String query = nameQuery.trim().toLowerCase();
        return studentStore.values().stream()
            .filter(s -> s.getName().toLowerCase().contains(query))
            .collect(Collectors.toList());
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * Updates a student's name, department, and/or marks.
     * Pass null or an empty string to keep the current value.
     *
     * @throws IllegalArgumentException if the student is not found or new values are invalid.
     */
    public void updateStudent(String id, String newName, String newDepartment, Double newMarks) {
        Student student = findById(id);

        String resolvedName  = (newName != null && !newName.trim().isEmpty())
                               ? newName.trim() : student.getName();
        String resolvedDept  = (newDepartment != null && !newDepartment.trim().isEmpty())
                               ? newDepartment.trim() : student.getDepartment();
        double resolvedMarks = (newMarks != null) ? newMarks : student.getMarks();

        Student.validate(id, resolvedName, resolvedDept, resolvedMarks);

        student.setName(resolvedName);
        student.setDepartment(resolvedDept);
        student.setMarks(resolvedMarks);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /**
     * Removes a student by ID.
     *
     * @throws IllegalArgumentException if the student is not found.
     */
    public void deleteStudent(String id) {
        findById(id); // validate existence
        studentStore.remove(id == null ? "" : id.trim());
    }

    /**
     * Returns the total number of students in the system.
     */
    public int count() {
        return studentStore.size();
    }
}
