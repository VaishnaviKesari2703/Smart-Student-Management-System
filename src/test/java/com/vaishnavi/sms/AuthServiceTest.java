package com.vaishnavi.sms;

import com.vaishnavi.sms.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.security.authentication.BadCredentialsException;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    @TempDir
    Path dir;
    TestContext ctx;

    @BeforeEach
    void setUp() {
        ctx = new TestContext(dir);
        ctx.userRepository.save(new User("USR-ADMIN", "admin", ctx.encoder.encode("Admin@123"), User.ROLE_ADMIN, null));
        com.vaishnavi.sms.dto.StudentRequest r = TestContext.request("22BCE1001", "Aarav Sharma");
        r.setPassword("Student@123");
        ctx.studentService.add(r);
    }

    @Test
    void validAdminLogin() {
        User u = ctx.authService.authenticate("admin", "Admin@123");
        assertEquals(User.ROLE_ADMIN, u.getRole());
    }

    @Test
    void validStudentLoginByUsernameOrRegistrationNumber() {
        assertEquals(User.ROLE_STUDENT, ctx.authService.authenticate("22bce1001", "Student@123").getRole());
        assertEquals(User.ROLE_STUDENT, ctx.authService.authenticate("22BCE1001", "Student@123").getRole());
    }

    @Test
    void invalidPasswordAndUnknownUserAreRejected() {
        assertThrows(BadCredentialsException.class, () -> ctx.authService.authenticate("admin", "wrong"));
        assertThrows(BadCredentialsException.class, () -> ctx.authService.authenticate("admin", null));
        assertThrows(BadCredentialsException.class, () -> ctx.authService.authenticate("ghost", "Admin@123"));
    }

    @Test
    void passwordsAreStoredHashedNotPlain() {
        String hash = ctx.userRepository.findByUsername("admin").orElseThrow().getPasswordHash();
        assertFalse(hash.contains("Admin@123"));
        assertTrue(hash.startsWith("$2"));
    }
}
