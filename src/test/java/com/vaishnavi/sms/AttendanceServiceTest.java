package com.vaishnavi.sms;

import com.vaishnavi.sms.dto.AttendanceSummary;
import com.vaishnavi.sms.exception.ApiExceptions.InvalidOperationException;
import com.vaishnavi.sms.model.AttendanceHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttendanceServiceTest {

    @TempDir
    Path dir;
    TestContext ctx;
    String id;

    @BeforeEach
    void setUp() {
        ctx = new TestContext(dir);
        id = ctx.studentService.add(TestContext.request("REG001", "Asha Rao")).getId();
    }

    @Test
    void percentageMatchesSpecificationExamples() {
        assertEquals(71.43, AttendanceSummary.calculatePercentage(30));
        assertEquals(73.81, AttendanceSummary.calculatePercentage(31));
        assertEquals(76.19, AttendanceSummary.calculatePercentage(32));
        assertEquals(83.33, AttendanceSummary.calculatePercentage(35));
        assertEquals(100.0, AttendanceSummary.calculatePercentage(42));
        assertEquals(0.0, AttendanceSummary.calculatePercentage(0));
    }

    @Test
    void minimumClassesRequiredIs32() {
        assertEquals(32, AttendanceSummary.MIN_CLASSES_REQUIRED);
        assertEquals(42, AttendanceSummary.TOTAL_CLASSES);
    }

    @Test
    void eligibleAt32AndShortageAt31() {
        AttendanceSummary at32 = AttendanceSummary.of(32);
        assertTrue(at32.isEligible());
        assertEquals("ELIGIBLE", at32.getStatus());
        assertEquals(0, at32.getAdditionalClassesRequired());

        AttendanceSummary at31 = AttendanceSummary.of(31);
        assertFalse(at31.isEligible());
        assertEquals("SHORTAGE", at31.getStatus());
        assertEquals(1, at31.getAdditionalClassesRequired());
        assertEquals(11, at31.getClassesMissed());
        assertEquals(12, AttendanceSummary.of(20).getAdditionalClassesRequired());
    }

    @Test
    void increaseAndDecreaseChangeByExactlyOne() {
        ctx.attendanceService.setAttendance(id, 30, "ADMIN_EDIT");
        assertEquals(31, ctx.attendanceService.increase(id).getClassesAttended());
        assertEquals(32, ctx.attendanceService.increase(id).getClassesAttended());
        assertEquals(31, ctx.attendanceService.decrease(id).getClassesAttended());
        assertEquals(31, ctx.attendanceService.getSummary(id).getClassesAttended());
    }

    @Test
    void attendanceCannotGoBelowZero() {
        assertThrows(InvalidOperationException.class, () -> ctx.attendanceService.decrease(id));
        assertEquals(0, ctx.attendanceService.getClassesAttended(id));
    }

    @Test
    void attendanceCannotExceed42() {
        ctx.attendanceService.setAttendance(id, 42, "ADMIN_EDIT");
        assertThrows(InvalidOperationException.class, () -> ctx.attendanceService.increase(id));
        assertThrows(InvalidOperationException.class, () -> ctx.attendanceService.setAttendance(id, 43, "ADMIN_EDIT"));
        assertThrows(InvalidOperationException.class, () -> ctx.attendanceService.setAttendance(id, -1, "ADMIN_EDIT"));
        assertEquals(42, ctx.attendanceService.getClassesAttended(id));
    }

    @Test
    void historyRecordsPreviousNewActionAndTime() {
        ctx.attendanceService.setAttendance(id, 30, "ADMIN_EDIT");
        ctx.attendanceService.increase(id);
        ctx.attendanceService.decrease(id);
        List<AttendanceHistory> history = ctx.attendanceService.getHistory(id);
        assertEquals(3, history.size());
        AttendanceHistory latest = history.get(0);
        assertEquals("DECREMENT", latest.getAction());
        assertEquals(31, latest.getPreviousAttendance());
        assertEquals(30, latest.getNewAttendance());
        assertTrue(latest.getTimestamp() != null);
    }

    @Test
    void attendancePersistsAcrossRestart() throws Exception {
        ctx.attendanceService.setAttendance(id, 30, "ADMIN_EDIT");
        ctx.attendanceService.increase(id);

        assertTrue(Files.readString(dir.resolve("attendance.json")).contains("31"));
        TestContext restarted = new TestContext(dir);
        assertEquals(31, restarted.attendanceService.getClassesAttended(id));
        assertEquals("REG001", restarted.studentService.get(id).getRegistrationNumber());
        assertEquals(2, restarted.attendanceService.getHistory(id).size());
    }

    @Test
    void corruptedJsonFileIsRecoveredGracefully() throws Exception {
        Files.writeString(dir.resolve("students.json"), "{ not valid json ]]");
        Files.writeString(dir.resolve("attendance.json"), "");
        TestContext recovered = new TestContext(dir);
        assertTrue(recovered.studentService.listAll().isEmpty());
        assertEquals("[]", Files.readString(dir.resolve("students.json")).trim());
        try (var files = Files.list(dir)) {
            assertTrue(files.anyMatch(p -> p.getFileName().toString().startsWith("students.json.corrupt-")));
        }
    }
}
