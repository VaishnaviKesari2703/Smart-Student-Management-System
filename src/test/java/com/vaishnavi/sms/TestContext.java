package com.vaishnavi.sms;

import com.vaishnavi.sms.repository.AttendanceRepository;
import com.vaishnavi.sms.repository.StudentRepository;
import com.vaishnavi.sms.repository.UserRepository;
import com.vaishnavi.sms.service.AttendanceService;
import com.vaishnavi.sms.service.AuthService;
import com.vaishnavi.sms.service.StudentService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Path;

/** Builds a complete service stack on top of an isolated data directory for unit tests. */
final class TestContext {

    final StudentRepository studentRepository;
    final UserRepository userRepository;
    final AttendanceRepository attendanceRepository;
    final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    final AttendanceService attendanceService;
    final StudentService studentService;
    final AuthService authService;

    TestContext(Path dir) {
        String d = dir.toString();
        studentRepository = new StudentRepository(d);
        userRepository = new UserRepository(d);
        attendanceRepository = new AttendanceRepository(d);
        attendanceService = new AttendanceService(attendanceRepository, studentRepository);
        studentService = new StudentService(studentRepository, userRepository, attendanceRepository,
                attendanceService, encoder);
        authService = new AuthService(userRepository, studentRepository, encoder);
    }

    static com.vaishnavi.sms.dto.StudentRequest request(String regNo, String name) {
        com.vaishnavi.sms.dto.StudentRequest r = new com.vaishnavi.sms.dto.StudentRequest();
        r.setRegistrationNumber(regNo);
        r.setFullName(name);
        r.setEmail(name.toLowerCase().replace(' ', '.') + "@college.edu");
        r.setDepartment("Computer Science");
        r.setYear(2);
        r.setSemester(3);
        return r;
    }
}
