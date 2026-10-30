package com.vaishnavi.sms.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

/**
 * Form login with BCrypt passwords, server-side sessions and role-based URL authorization.
 * CSRF protection is disabled because this is a local demo that uses same-origin JSON calls.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String ADMIN_HOME = "/admin/dashboard.html";
    public static final String STUDENT_HOME = "/student/dashboard.html";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationSuccessHandler roleRedirect = (request, response, authentication) -> {
            boolean admin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals);
            response.sendRedirect(request.getContextPath() + (admin ? ADMIN_HOME : STUDENT_HOME));
        };
        LogoutSuccessHandler logoutRedirect =
                (request, response, authentication) -> response.sendRedirect("/login.html?logout");

        AntPathRequestMatcher apiMatcher = new AntPathRequestMatcher("/api/**");
        AuthenticationEntryPoint pageEntry = new LoginUrlAuthenticationEntryPoint("/login.html");
        AuthenticationEntryPoint apiEntry = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login.html", "/css/**", "/js/**", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/admin/**", "/api/admin/**", "/api/students/**", "/api/attendance/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/student/**", "/api/me/**").hasRole("STUDENT")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .successHandler(roleRedirect)
                        .failureUrl("/login.html?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessHandler(logoutRedirect)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> {
                            if (apiMatcher.matches(request)) {
                                apiEntry.commence(request, response, e);
                            } else {
                                pageEntry.commence(request, response, e);
                            }
                        })
                        .accessDeniedHandler(this::handleDenied))
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    private void handleDenied(HttpServletRequest request, HttpServletResponse response,
                              org.springframework.security.access.AccessDeniedException e) throws IOException {
        if (request.getRequestURI().startsWith("/api/")) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\","
                    + "\"message\":\"You are not allowed to access this resource.\"}");
        } else {
            response.sendRedirect("/");
        }
    }
}
