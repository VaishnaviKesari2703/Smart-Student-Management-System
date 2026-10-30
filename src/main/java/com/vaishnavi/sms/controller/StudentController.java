package com.vaishnavi.sms.controller;

import com.vaishnavi.sms.dto.StudentRequest;
import com.vaishnavi.sms.dto.StudentView;
import com.vaishnavi.sms.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Admin-only student CRUD, search and filter API. */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public List<StudentView> list(@RequestParam(required = false) String q,
                                  @RequestParam(required = false) String department,
                                  @RequestParam(required = false) Integer semester,
                                  @RequestParam(required = false) String status) {
        return studentService.search(q, department, semester, status);
    }

    @GetMapping("/{id}")
    public StudentView get(@PathVariable String id) {
        return studentService.get(id);
    }

    @PostMapping
    public ResponseEntity<StudentView> create(@RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.add(request));
    }

    @PutMapping("/{id}")
    public StudentView update(@PathVariable String id, @RequestBody StudentRequest request) {
        return studentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
