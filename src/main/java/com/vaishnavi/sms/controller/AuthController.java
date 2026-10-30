package com.vaishnavi.sms.controller;

import com.vaishnavi.sms.model.User;
import com.vaishnavi.sms.service.AuthService;
import com.vaishnavi.sms.service.StudentService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** Session information for the logged-in user, plus the root redirect. */
@RestController
public class AuthController {

    private final AuthService authService;
    private final StudentService studentService;

    public AuthController(AuthService authService, StudentService studentService) {
        this.authService = authService;
        this.studentService = studentService;
    }

    /** "/" sends users to the dashboard of their role. */
    @GetMapping("/")
    public org.springframework.http.ResponseEntity<Void> home(Authentication auth) {
        boolean admin = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        return org.springframework.http.ResponseEntity.status(302)
                .header("Location", admin ? "/admin/dashboard.html" : "/student/dashboard.html").build();
    }

    @GetMapping("/api/auth/me")
    public Map<String, Object> me(Authentication auth) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("username", auth.getName());
        boolean admin = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        m.put("role", admin ? User.ROLE_ADMIN : User.ROLE_STUDENT);
        String studentId = authService.studentIdFor(auth.getName());
        m.put("studentId", studentId);
        m.put("displayName", studentId == null ? "Administrator" : studentService.get(studentId).getFullName());
        return m;
    }
}
