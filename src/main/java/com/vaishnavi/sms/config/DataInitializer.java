package com.vaishnavi.sms.config;

import com.vaishnavi.sms.dto.StudentRequest;
import com.vaishnavi.sms.model.User;
import com.vaishnavi.sms.repository.StudentRepository;
import com.vaishnavi.sms.repository.UserRepository;
import com.vaishnavi.sms.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** On first start (no users stored yet) creates the demo admin and sample students. */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentService studentService;
    private final PasswordEncoder passwordEncoder;
    private final boolean seed;

    public DataInitializer(UserRepository userRepository, StudentRepository studentRepository,
                           StudentService studentService, PasswordEncoder passwordEncoder,
                           @Value("${app.seed-demo-data:true}") boolean seed) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.studentService = studentService;
        this.passwordEncoder = passwordEncoder;
        this.seed = seed;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seed || !userRepository.findAll().isEmpty()) {
            return;
        }
        log.info("No users found - creating demo admin and sample students.");
        userRepository.save(new User("USR-ADMIN", "admin", passwordEncoder.encode("Admin@123"), User.ROLE_ADMIN, null));
        if (!studentRepository.findAll().isEmpty()) {
            return;
        }
        addStudent("22BCE1001", "Aarav Sharma", "aarav.sharma@college.edu", "Computer Science", 3, 5, 30);
        addStudent("22BCE1002", "Priya Nair", "priya.nair@college.edu", "Computer Science", 3, 5, 35);
        addStudent("22BEC2001", "Rohan Mehta", "rohan.mehta@college.edu", "Electronics", 2, 3, 31);
        addStudent("22BME3001", "Ananya Iyer", "ananya.iyer@college.edu", "Mechanical", 4, 7, 42);
        addStudent("22BIT4001", "Karthik Reddy", "karthik.reddy@college.edu", "Information Technology", 1, 2, 32);
        addStudent("22BIT4002", "Sneha Kapoor", "sneha.kapoor@college.edu", "Information Technology", 1, 2, 20);
    }

    private void addStudent(String regNo, String name, String email, String dept, int year, int sem, int attended) {
        StudentRequest r = new StudentRequest();
        r.setRegistrationNumber(regNo);
        r.setFullName(name);
        r.setEmail(email);
        r.setDepartment(dept);
        r.setYear(year);
        r.setSemester(sem);
        r.setUsername(regNo.toLowerCase());
        r.setClassesAttended(attended);
        studentService.add(r);
    }
}
