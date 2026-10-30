package com.vaishnavi.sms.service;

import com.vaishnavi.sms.model.Student;
import com.vaishnavi.sms.model.User;
import com.vaishnavi.sms.repository.StudentRepository;
import com.vaishnavi.sms.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** Authentication logic: users sign in with a username or a student registration number. */
@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, StudentRepository studentRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Finds an account by username, falling back to a student registration number. */
    public Optional<User> findUser(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        String id = identifier.trim();
        Optional<User> byUsername = userRepository.findByUsername(id);
        if (byUsername.isPresent()) {
            return byUsername;
        }
        return studentRepository.findByRegistrationNumber(id)
                .map(Student::getId)
                .flatMap(userRepository::findByStudentId);
    }

    /** Verifies credentials; throws BadCredentialsException when they are wrong. */
    public User authenticate(String identifier, String rawPassword) {
        User user = findUser(identifier).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (rawPassword == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = findUser(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
    }

    /** Student id linked to the logged-in username (null for admin). */
    public String studentIdFor(String username) {
        return userRepository.findByUsername(username).map(User::getStudentId).orElse(null);
    }
}
