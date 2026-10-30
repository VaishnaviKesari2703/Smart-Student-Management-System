package com.vaishnavi.sms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end API tests over the real security configuration and JSON files. */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    static Path dataDir;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) throws IOException {
        dataDir = Files.createTempDirectory("sms-it-");
        registry.add("app.data-dir", () -> dataDir.toString());
    }

    @Autowired
    MockMvc mvc;

    private MockHttpSession login(String user, String pass) throws Exception {
        var result = mvc.perform(post("/login").param("username", user).param("password", pass)).andReturn();
        return (MockHttpSession) result.getRequest().getSession();
    }

    @Test
    void anonymousUsersAreBlockedFromApiAndPages() throws Exception {
        mvc.perform(get("/api/students")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/me/attendance")).andExpect(status().isUnauthorized());
        mvc.perform(get("/admin/dashboard.html")).andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/login.html")));
        mvc.perform(get("/login.html")).andExpect(status().isOk());
    }

    @Test
    void adminLoginRedirectsToAdminDashboardAndSeesAllStudents() throws Exception {
        mvc.perform(post("/login").param("username", "admin").param("password", "Admin@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/admin/dashboard.html"));
        MockHttpSession s = login("admin", "Admin@123");
        mvc.perform(get("/api/students").session(s)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(5)));
        mvc.perform(get("/api/admin/stats").session(s)).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClasses").value(42));
    }

    @Test
    void invalidPasswordIsRejected() throws Exception {
        mvc.perform(post("/login").param("username", "admin").param("password", "bad"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login.html?error"));
    }

    @Test
    void studentLoginGoesToStudentDashboardAndCannotUseAdminApis() throws Exception {
        mvc.perform(post("/login").param("username", "22BCE1001").param("password", "Student@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/student/dashboard.html"));
        MockHttpSession s = login("22BCE1001", "Student@123");
        mvc.perform(get("/api/students").session(s)).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/stats").session(s)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/students/STU002").session(s)).andExpect(status().isForbidden());
        mvc.perform(get("/admin/dashboard.html").session(s)).andExpect(status().is3xxRedirection());
        mvc.perform(get("/api/me/student").session(s)).andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("22BCE1001"));
    }

    @Test
    void adminCannotUseStudentSelfServiceApis() throws Exception {
        MockHttpSession s = login("admin", "Admin@123");
        mvc.perform(post("/api/me/attendance/increase").session(s)).andExpect(status().isForbidden());
    }

    @Test
    void studentPlusAndMinusAreStoredAndVisibleToAdmin() throws Exception {
        MockHttpSession student = login("22BIT4002", "Student@123");
        MockHttpSession admin = login("admin", "Admin@123");

        mvc.perform(get("/api/me/attendance").session(student)).andExpect(jsonPath("$.classesAttended").value(20));
        mvc.perform(post("/api/me/attendance/increase").session(student)).andExpect(status().isOk())
                .andExpect(jsonPath("$.classesAttended").value(21))
                .andExpect(jsonPath("$.classesMissed").value(21));
        mvc.perform(post("/api/me/attendance/decrease").session(student)).andExpect(jsonPath("$.classesAttended").value(20));
        mvc.perform(post("/api/me/attendance/increase").session(student)).andExpect(jsonPath("$.classesAttended").value(21));

        mvc.perform(get("/api/attendance").session(admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.registrationNumber=='22BIT4002')].attendance.classesAttended").value(21));
        mvc.perform(get("/api/me/attendance/history").session(student)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("INCREMENT"))
                .andExpect(jsonPath("$[0].previousAttendance").value(20))
                .andExpect(jsonPath("$[0].newAttendance").value(21));
    }

    @Test
    void adminStudentCrudOverRest() throws Exception {
        MockHttpSession admin = login("admin", "Admin@123");
        String body = "{\"registrationNumber\":\"IT9001\",\"fullName\":\"Test Student\",\"email\":\"t@college.edu\","
                + "\"department\":\"Civil\",\"year\":2,\"semester\":4,\"classesAttended\":33}";
        mvc.perform(post("/api/students").session(admin).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.attendance.status").value("ELIGIBLE"));
        mvc.perform(post("/api/students").session(admin).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/students").session(admin).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.fullName").exists());

        String id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(
                mvc.perform(get("/api/students").session(admin).param("q", "IT9001")).andReturn()
                        .getResponse().getContentAsString()).get(0).get("id").asText();
        mvc.perform(put("/api/students/" + id).session(admin).contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("Test Student", "Renamed Student")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Renamed Student"));
        mvc.perform(delete("/api/students/" + id).session(admin)).andExpect(status().isNoContent());
        mvc.perform(get("/api/students/" + id).session(admin)).andExpect(status().isNotFound());
    }
}
