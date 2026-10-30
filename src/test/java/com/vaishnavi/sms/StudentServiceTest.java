package com.vaishnavi.sms;

import com.vaishnavi.sms.dto.StudentRequest;
import com.vaishnavi.sms.dto.StudentView;
import com.vaishnavi.sms.exception.ApiExceptions.DuplicateResourceException;
import com.vaishnavi.sms.exception.ApiExceptions.ResourceNotFoundException;
import com.vaishnavi.sms.exception.ApiExceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentServiceTest {

    @TempDir
    Path dir;
    TestContext ctx;

    @BeforeEach
    void setUp() {
        ctx = new TestContext(dir);
    }

    @Test
    void addStudentPersistsStudentUserAndAttendance() {
        StudentView v = ctx.studentService.add(TestContext.request("REG001", "Asha Rao"));
        assertEquals("STU001", v.getId());
        assertEquals("reg001", v.getUsername());
        assertEquals(0, v.getAttendance().getClassesAttended());
        assertTrue(ctx.userRepository.findByStudentId("STU001").isPresent());
        assertNotEquals(StudentServiceHolder.DEFAULT,
                ctx.userRepository.findByStudentId("STU001").get().getPasswordHash(), "password must be hashed");
    }

    @Test
    void searchByNameRegistrationDepartmentAndSemester() {
        ctx.studentService.add(TestContext.request("REG001", "Asha Rao"));
        StudentRequest other = TestContext.request("ECE777", "Vikram Das");
        other.setDepartment("Electronics");
        other.setSemester(5);
        ctx.studentService.add(other);

        assertEquals(1, ctx.studentService.search("asha", null, null, null).size());
        assertEquals(1, ctx.studentService.search("ece7", null, null, null).size());
        assertEquals(1, ctx.studentService.search(null, "electronics", null, null).size());
        assertEquals(1, ctx.studentService.search(null, null, 5, null).size());
        assertEquals(0, ctx.studentService.search("nobody", null, null, null).size());
        assertEquals(2, ctx.studentService.search("", null, null, null).size());
    }

    @Test
    void updateStudentChangesFieldsAndPassword() {
        StudentView v = ctx.studentService.add(TestContext.request("REG001", "Asha Rao"));
        String oldHash = ctx.userRepository.findByStudentId(v.getId()).get().getPasswordHash();

        StudentRequest upd = TestContext.request("REG001", "Asha R Updated");
        upd.setSemester(6);
        upd.setPassword("NewPass@1");
        StudentView updated = ctx.studentService.update(v.getId(), upd);

        assertEquals("Asha R Updated", updated.getFullName());
        assertEquals(6, ctx.studentService.get(v.getId()).getSemester());
        assertNotEquals(oldHash, ctx.userRepository.findByStudentId(v.getId()).get().getPasswordHash());
    }

    @Test
    void updateUnknownStudentFails() {
        assertThrows(ResourceNotFoundException.class,
                () -> ctx.studentService.update("STU999", TestContext.request("X1", "Nobody Here")));
    }

    @Test
    void deleteStudentRemovesAllRelatedRecords() {
        StudentView v = ctx.studentService.add(TestContext.request("REG001", "Asha Rao"));
        ctx.attendanceService.increase(v.getId());
        ctx.studentService.delete(v.getId());

        assertThrows(ResourceNotFoundException.class, () -> ctx.studentService.get(v.getId()));
        assertTrue(ctx.userRepository.findByStudentId(v.getId()).isEmpty());
        assertTrue(ctx.attendanceRepository.findByStudentId(v.getId()).isEmpty());
        assertTrue(ctx.attendanceRepository.findHistory(v.getId()).isEmpty());
        assertThrows(ResourceNotFoundException.class, () -> ctx.studentService.delete(v.getId()));
    }

    @Test
    void duplicateRegistrationNumberIsRejected() {
        ctx.studentService.add(TestContext.request("REG001", "Asha Rao"));
        assertThrows(DuplicateResourceException.class,
                () -> ctx.studentService.add(TestContext.request("reg001", "Someone Else")));
        assertEquals(1, ctx.studentService.listAll().size());
    }

    @Test
    void requiredFieldsAreValidated() {
        StudentRequest empty = new StudentRequest();
        ValidationException ex = assertThrows(ValidationException.class, () -> ctx.studentService.add(empty));
        List<String> fields = List.copyOf(ex.getFieldErrors().keySet());
        assertTrue(fields.containsAll(List.of("registrationNumber", "fullName", "email", "department", "year", "semester")));

        StudentRequest badEmail = TestContext.request("REG002", "Bad Email");
        badEmail.setEmail("not-an-email");
        assertTrue(assertThrows(ValidationException.class, () -> ctx.studentService.add(badEmail))
                .getFieldErrors().containsKey("email"));
        assertFalse(ctx.studentRepository.findByRegistrationNumber("REG002").isPresent());
    }

    /** Holder so the test reads clearly: the plain default password must never be stored. */
    private static final class StudentServiceHolder {
        static final String DEFAULT = com.vaishnavi.sms.service.StudentService.DEFAULT_PASSWORD;
    }
}
