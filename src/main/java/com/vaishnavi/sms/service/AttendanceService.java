package com.vaishnavi.sms.service;

import com.vaishnavi.sms.dto.AttendanceSummary;
import com.vaishnavi.sms.exception.ApiExceptions.InvalidOperationException;
import com.vaishnavi.sms.exception.ApiExceptions.ResourceNotFoundException;
import com.vaishnavi.sms.model.Attendance;
import com.vaishnavi.sms.model.AttendanceHistory;
import com.vaishnavi.sms.repository.AttendanceRepository;
import com.vaishnavi.sms.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Attendance rules, calculations and persistence. */
@Service
public class AttendanceService {

    public static final String ACTION_INCREMENT = "INCREMENT";
    public static final String ACTION_DECREMENT = "DECREMENT";
    public static final String ACTION_ADMIN_EDIT = "ADMIN_EDIT";

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    public int getClassesAttended(String studentId) {
        return attendanceRepository.findByStudentId(studentId).map(Attendance::getClassesAttended).orElse(0);
    }

    public AttendanceSummary getSummary(String studentId) {
        requireStudent(studentId);
        return AttendanceSummary.of(getClassesAttended(studentId));
    }

    /** +1 class. Rejected when already at 42. */
    public synchronized AttendanceSummary increase(String studentId) {
        return adjust(studentId, +1, ACTION_INCREMENT);
    }

    /** -1 class. Rejected when already at 0. */
    public synchronized AttendanceSummary decrease(String studentId) {
        return adjust(studentId, -1, ACTION_DECREMENT);
    }

    /** Sets an absolute value (admin edit / initial value); validates the 0..42 range. */
    public synchronized AttendanceSummary setAttendance(String studentId, int value, String action) {
        requireStudent(studentId);
        validateRange(value);
        int previous = getClassesAttended(studentId);
        if (previous != value) {
            persist(studentId, previous, value, action);
        } else if (attendanceRepository.findByStudentId(studentId).isEmpty()) {
            attendanceRepository.save(new Attendance(studentId, value));
        }
        return AttendanceSummary.of(value);
    }

    public List<AttendanceHistory> getHistory(String studentId) {
        requireStudent(studentId);
        return attendanceRepository.findHistory(studentId);
    }

    private AttendanceSummary adjust(String studentId, int delta, String action) {
        requireStudent(studentId);
        int previous = getClassesAttended(studentId);
        int updated = previous + delta;
        if (updated < 0) {
            throw new InvalidOperationException("Attendance cannot go below 0.");
        }
        if (updated > AttendanceSummary.TOTAL_CLASSES) {
            throw new InvalidOperationException("Attendance cannot exceed " + AttendanceSummary.TOTAL_CLASSES + ".");
        }
        persist(studentId, previous, updated, action);
        return AttendanceSummary.of(updated);
    }

    private void persist(String studentId, int previous, int updated, String action) {
        attendanceRepository.saveWithHistory(
                new Attendance(studentId, updated),
                new AttendanceHistory(UUID.randomUUID().toString(), studentId, previous, updated,
                        action, LocalDateTime.now()));
    }

    private void validateRange(int value) {
        if (value < 0 || value > AttendanceSummary.TOTAL_CLASSES) {
            throw new InvalidOperationException(
                    "Classes attended must be between 0 and " + AttendanceSummary.TOTAL_CLASSES + ".");
        }
    }

    private void requireStudent(String studentId) {
        if (studentId == null || studentRepository.findById(studentId).isEmpty()) {
            throw new ResourceNotFoundException("Student not found: " + studentId);
        }
    }
}
