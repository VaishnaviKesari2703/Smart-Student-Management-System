package com.vaishnavi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for StudentService.
 * Covers: add, retrieve, name search, invalid marks, duplicate IDs,
 *         update, delete, and missing-student handling.
 */
class StudentServiceTest {

    private StudentService service;

    @BeforeEach
    void setUp() {
        service = new StudentService();
    }

    // ── Add & Retrieve ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Add student and retrieve by ID")
    void testAddAndFindById() {
        service.addStudent("S001", "Alice Kumar", "Computer Science", 88.0);
        Student student = service.findById("S001");

        assertEquals("S001", student.getId());
        assertEquals("Alice Kumar", student.getName());
        assertEquals("Computer Science", student.getDepartment());
        assertEquals(88.0, student.getMarks(), 0.001);
        assertEquals("A", student.getGrade());
    }

    @Test
    @DisplayName("View all students returns correct count")
    void testGetAllStudents() {
        service.addStudent("S001", "Alice Kumar",   "Computer Science", 88.0);
        service.addStudent("S002", "Bob Sharma",    "Electronics",      72.5);
        service.addStudent("S003", "Charlie Patel", "Mechanical",       55.0);

        List<Student> all = service.getAllStudents();
        assertEquals(3, all.size());
    }

    // ── Name Search ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Search by partial name returns matching students")
    void testSearchByName() {
        service.addStudent("S001", "Alice Kumar",  "Computer Science", 88.0);
        service.addStudent("S002", "Alicia Mehta", "Electronics",      70.0);
        service.addStudent("S003", "Bob Sharma",   "Mechanical",       65.0);

        List<Student> results = service.findByName("alic");
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Search by name with no match returns empty list")
    void testSearchByNameNoMatch() {
        service.addStudent("S001", "Alice Kumar", "Computer Science", 88.0);
        List<Student> results = service.findByName("Zara");
        assertTrue(results.isEmpty());
    }

    // ── Invalid Marks ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Reject marks below 0")
    void testRejectNegativeMarks() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.addStudent("S010", "Test User", "CS", -5.0));
        assertTrue(ex.getMessage().contains("Marks must be between 0 and 100"));
    }

    @Test
    @DisplayName("Reject marks above 100")
    void testRejectMarksAbove100() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.addStudent("S011", "Test User", "CS", 105.0));
        assertTrue(ex.getMessage().contains("Marks must be between 0 and 100"));
    }

    @Test
    @DisplayName("Accept boundary marks 0 and 100")
    void testBoundaryMarksAccepted() {
        assertDoesNotThrow(() -> service.addStudent("S020", "Zero Score", "Arts",    0.0));
        assertDoesNotThrow(() -> service.addStudent("S021", "Full Score", "Science", 100.0));

        assertEquals("F",  service.findById("S020").getGrade());
        assertEquals("A+", service.findById("S021").getGrade());
    }

    // ── Duplicate IDs ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Reject duplicate student ID")
    void testRejectDuplicateId() {
        service.addStudent("S001", "Alice Kumar", "Computer Science", 88.0);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.addStudent("S001", "Another Person", "Physics", 60.0));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Update student name and marks")
    void testUpdateStudent() {
        service.addStudent("S001", "Alice Kumar", "Computer Science", 88.0);
        service.updateStudent("S001", "Alice K.", null, 92.0);

        Student updated = service.findById("S001");
        assertEquals("Alice K.", updated.getName());
        assertEquals("Computer Science", updated.getDepartment()); // unchanged
        assertEquals(92.0, updated.getMarks(), 0.001);
        assertEquals("A+", updated.getGrade());
    }

    @Test
    @DisplayName("Partial update keeps unchanged fields")
    void testPartialUpdate() {
        service.addStudent("S002", "Bob Sharma", "Electronics", 72.5);
        service.updateStudent("S002", null, "Electrical Engineering", null);

        Student updated = service.findById("S002");
        assertEquals("Bob Sharma", updated.getName());              // unchanged
        assertEquals("Electrical Engineering", updated.getDepartment());
        assertEquals(72.5, updated.getMarks(), 0.001);             // unchanged
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Delete student reduces count")
    void testDeleteStudent() {
        service.addStudent("S001", "Alice Kumar", "CS", 88.0);
        service.addStudent("S002", "Bob Sharma",  "EC", 72.0);
        assertEquals(2, service.count());

        service.deleteStudent("S001");
        assertEquals(1, service.count());
    }

    @Test
    @DisplayName("Deleted student cannot be found")
    void testDeletedStudentNotFound() {
        service.addStudent("S001", "Alice Kumar", "CS", 88.0);
        service.deleteStudent("S001");

        assertThrows(IllegalArgumentException.class,
            () -> service.findById("S001"));
    }

    // ── Missing Student ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Finding non-existent student throws exception")
    void testFindNonExistentStudent() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.findById("NONEXISTENT"));
        assertTrue(ex.getMessage().contains("No student found"));
    }

    @Test
    @DisplayName("Updating non-existent student throws exception")
    void testUpdateNonExistentStudent() {
        assertThrows(IllegalArgumentException.class,
            () -> service.updateStudent("GHOST", "Name", "Dept", 50.0));
    }

    @Test
    @DisplayName("Deleting non-existent student throws exception")
    void testDeleteNonExistentStudent() {
        assertThrows(IllegalArgumentException.class,
            () -> service.deleteStudent("GHOST"));
    }

    // ── Grade Boundary Tests ──────────────────────────────────────────────────

    @Test
    @DisplayName("Grade calculation covers all bands")
    void testGradeCalculation() {
        service.addStudent("G1", "Student A+", "Dept", 95.0);
        service.addStudent("G2", "Student A",  "Dept", 85.0);
        service.addStudent("G3", "Student B",  "Dept", 75.0);
        service.addStudent("G4", "Student C",  "Dept", 65.0);
        service.addStudent("G5", "Student D",  "Dept", 55.0);
        service.addStudent("G6", "Student F",  "Dept", 40.0);

        assertEquals("A+", service.findById("G1").getGrade());
        assertEquals("A",  service.findById("G2").getGrade());
        assertEquals("B",  service.findById("G3").getGrade());
        assertEquals("C",  service.findById("G4").getGrade());
        assertEquals("D",  service.findById("G5").getGrade());
        assertEquals("F",  service.findById("G6").getGrade());
    }

    // ── Validation Edge Cases ─────────────────────────────────────────────────

    @Test
    @DisplayName("Empty student ID is rejected")
    void testEmptyIdRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> service.addStudent("", "Name", "Dept", 50.0));
    }

    @Test
    @DisplayName("Empty student name is rejected")
    void testEmptyNameRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> service.addStudent("S999", "", "Dept", 50.0));
    }
}
