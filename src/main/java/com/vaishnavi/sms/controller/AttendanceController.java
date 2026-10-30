package com.vaishnavi.sms.controller;

import com.vaishnavi.sms.dto.AttendanceSummary;
import com.vaishnavi.sms.dto.StudentView;
import com.vaishnavi.sms.model.AttendanceHistory;
import com.vaishnavi.sms.service.AttendanceService;
import com.vaishnavi.sms.service.AuthService;
import com.vaishnavi.sms.service.StudentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Attendance API.
 * <ul>
 *   <li>{@code /api/me/**} - STUDENT only; always acts on the logged-in student's own record.</li>
 *   <li>{@code /api/attendance/**} - ADMIN only monitoring.</li>
 * </ul>
 * The + / - adjustments are simulated demo controls, not verified classroom attendance.
 */
@RestController
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final StudentService studentService;
    private final AuthService authService;

    public AttendanceController(AttendanceService attendanceService, StudentService studentService,
                                AuthService authService) {
        this.attendanceService = attendanceService;
        this.studentService = studentService;
        this.authService = authService;
    }

    // ---- student (self) ----

    @GetMapping("/api/me/student")
    public StudentView myProfile(Authentication auth) {
        return studentService.get(ownId(auth));
    }

    @GetMapping("/api/me/attendance")
    public AttendanceSummary myAttendance(Authentication auth) {
        return attendanceService.getSummary(ownId(auth));
    }

    @PostMapping("/api/me/attendance/increase")
    public AttendanceSummary increase(Authentication auth) {
        return attendanceService.increase(ownId(auth));
    }

    @PostMapping("/api/me/attendance/decrease")
    public AttendanceSummary decrease(Authentication auth) {
        return attendanceService.decrease(ownId(auth));
    }

    @GetMapping("/api/me/attendance/history")
    public List<AttendanceHistory> myHistory(Authentication auth) {
        return attendanceService.getHistory(ownId(auth));
    }

    // ---- admin monitoring ----

    @GetMapping("/api/attendance")
    public List<StudentView> all() {
        return studentService.listAll();
    }

    @GetMapping("/api/attendance/shortage")
    public List<StudentView> shortage() {
        return studentService.search(null, null, null, "SHORTAGE");
    }

    @GetMapping("/api/attendance/{studentId}/history")
    public List<AttendanceHistory> history(@PathVariable String studentId) {
        return attendanceService.getHistory(studentId);
    }

    private String ownId(Authentication auth) {
        String id = authService.studentIdFor(auth.getName());
        if (id == null) {
            throw new org.springframework.security.access.AccessDeniedException("No student record linked to this account.");
        }
        return id;
    }
}
