package com.vaishnavi.sms.service;

import com.vaishnavi.sms.dto.AttendanceSummary;
import com.vaishnavi.sms.dto.StudentRequest;
import com.vaishnavi.sms.dto.StudentView;
import com.vaishnavi.sms.exception.ApiExceptions.DuplicateResourceException;
import com.vaishnavi.sms.exception.ApiExceptions.ResourceNotFoundException;
import com.vaishnavi.sms.exception.ApiExceptions.ValidationException;
import com.vaishnavi.sms.model.Student;
import com.vaishnavi.sms.model.User;
import com.vaishnavi.sms.repository.AttendanceRepository;
import com.vaishnavi.sms.repository.StudentRepository;
import com.vaishnavi.sms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/** Student CRUD, search/filter and dashboard statistics. */
@Service
public class StudentService {

    public static final String DEFAULT_PASSWORD = "Student@123";
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceService attendanceService;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository, UserRepository userRepository,
                          AttendanceRepository attendanceRepository, AttendanceService attendanceService,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
        this.attendanceService = attendanceService;
        this.passwordEncoder = passwordEncoder;
    }

    public synchronized StudentView add(StudentRequest req) {
        validate(req, true);
        String regNo = req.getRegistrationNumber().trim();
        if (studentRepository.findByRegistrationNumber(regNo).isPresent()) {
            throw new DuplicateResourceException("Registration number already exists: " + regNo);
        }
        String username = isBlank(req.getUsername()) ? regNo.toLowerCase(Locale.ROOT) : req.getUsername().trim();
        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateResourceException("Username already exists: " + username);
        }

        Student s = new Student();
        s.setId(studentRepository.nextId());
        applyFields(s, req);
        s.setUsername(username);
        studentRepository.save(s);

        String password = isBlank(req.getPassword()) ? DEFAULT_PASSWORD : req.getPassword();
        userRepository.save(new User("USR-" + s.getId(), username, passwordEncoder.encode(password),
                User.ROLE_STUDENT, s.getId()));

        attendanceService.setAttendance(s.getId(),
                req.getClassesAttended() == null ? 0 : req.getClassesAttended(),
                AttendanceService.ACTION_ADMIN_EDIT);
        return toView(s);
    }

    public synchronized StudentView update(String id, StudentRequest req) {
        Student s = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        validate(req, false);
        String regNo = req.getRegistrationNumber().trim();
        studentRepository.findByRegistrationNumber(regNo).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new DuplicateResourceException("Registration number already exists: " + regNo);
            }
        });
        User user = userRepository.findByStudentId(id).orElse(null);
        String username = isBlank(req.getUsername()) ? s.getUsername() : req.getUsername().trim();
        userRepository.findByUsername(username).ifPresent(other -> {
            if (!other.getStudentId().equals(id)) {
                throw new DuplicateResourceException("Username already exists: " + username);
            }
        });

        applyFields(s, req);
        s.setUsername(username);
        studentRepository.save(s);

        if (user != null) {
            user.setUsername(username);
            if (!isBlank(req.getPassword())) {
                user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
            }
            userRepository.save(user);
        }
        if (req.getClassesAttended() != null) {
            attendanceService.setAttendance(id, req.getClassesAttended(), AttendanceService.ACTION_ADMIN_EDIT);
        }
        return toView(s);
    }

    public synchronized void delete(String id) {
        if (!studentRepository.deleteById(id)) {
            throw new ResourceNotFoundException("Student not found: " + id);
        }
        userRepository.deleteByStudentId(id);
        attendanceRepository.deleteByStudentId(id);
    }

    public StudentView get(String id) {
        return studentRepository.findById(id).map(this::toView)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    public List<StudentView> listAll() {
        return search(null, null, null, null);
    }

    /**
     * Search by registration number or name (q), filter by department, semester and status
     * (ELIGIBLE / SHORTAGE). All parameters are optional.
     */
    public List<StudentView> search(String q, String department, Integer semester, String status) {
        String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        return studentRepository.findAll().stream()
                .filter(s -> query.isEmpty()
                        || s.getRegistrationNumber().toLowerCase(Locale.ROOT).contains(query)
                        || s.getFullName().toLowerCase(Locale.ROOT).contains(query))
                .filter(s -> isBlank(department) || s.getDepartment().equalsIgnoreCase(department.trim()))
                .filter(s -> semester == null || s.getSemester() == semester)
                .map(this::toView)
                .filter(v -> isBlank(status) || v.getAttendance().getStatus().equalsIgnoreCase(status.trim()))
                .sorted(Comparator.comparing(StudentView::getRegistrationNumber, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /** Dashboard statistics, always computed from the stored records. */
    public Map<String, Object> stats() {
        List<StudentView> all = listAll();
        long eligible = all.stream().filter(v -> v.getAttendance().isEligible()).count();
        double average = all.stream().mapToDouble(v -> v.getAttendance().getPercentage()).average().orElse(0);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalStudents", all.size());
        m.put("totalClasses", AttendanceSummary.TOTAL_CLASSES);
        m.put("minimumPercentage", AttendanceSummary.MIN_PERCENTAGE);
        m.put("minimumClassesRequired", AttendanceSummary.MIN_CLASSES_REQUIRED);
        m.put("eligibleStudents", eligible);
        m.put("shortageStudents", all.size() - eligible);
        m.put("averageAttendance", Math.round(average * 100.0) / 100.0);
        return m;
    }

    private StudentView toView(Student s) {
        return new StudentView(s, AttendanceSummary.of(attendanceService.getClassesAttended(s.getId())));
    }

    private void applyFields(Student s, StudentRequest req) {
        s.setRegistrationNumber(req.getRegistrationNumber().trim());
        s.setFullName(req.getFullName().trim());
        s.setEmail(req.getEmail().trim());
        s.setDepartment(req.getDepartment().trim());
        s.setYear(req.getYear());
        s.setSemester(req.getSemester());
    }

    private void validate(StudentRequest req, boolean creating) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (req == null) {
            errors.put("request", "Student details are required.");
            throw new ValidationException(errors);
        }
        if (isBlank(req.getRegistrationNumber())) {
            errors.put("registrationNumber", "Registration number is required.");
        }
        if (isBlank(req.getFullName())) {
            errors.put("fullName", "Full name is required.");
        }
        if (isBlank(req.getEmail())) {
            errors.put("email", "Email is required.");
        } else if (!EMAIL.matcher(req.getEmail().trim()).matches()) {
            errors.put("email", "Email format is invalid.");
        }
        if (isBlank(req.getDepartment())) {
            errors.put("department", "Department is required.");
        }
        if (req.getYear() == null || req.getYear() < 1 || req.getYear() > 6) {
            errors.put("year", "Year must be between 1 and 6.");
        }
        if (req.getSemester() == null || req.getSemester() < 1 || req.getSemester() > 12) {
            errors.put("semester", "Semester must be between 1 and 12.");
        }
        if (!isBlank(req.getPassword()) && req.getPassword().length() < 6) {
            errors.put("password", "Password must be at least 6 characters.");
        }
        if (req.getClassesAttended() != null
                && (req.getClassesAttended() < 0 || req.getClassesAttended() > AttendanceSummary.TOTAL_CLASSES)) {
            errors.put("classesAttended", "Classes attended must be between 0 and " + AttendanceSummary.TOTAL_CLASSES + ".");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
