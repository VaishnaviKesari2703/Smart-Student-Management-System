package com.vaishnavi.sms.controller;

import com.vaishnavi.sms.service.StudentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Admin dashboard statistics, calculated live from the JSON records. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final StudentService studentService;

    public AdminController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return studentService.stats();
    }
}
