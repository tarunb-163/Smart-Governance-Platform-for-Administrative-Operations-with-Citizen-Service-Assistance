package com.civicpulse.controller;

import com.civicpulse.model.Citizen;
import com.civicpulse.repository.CitizenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class CitizenAuthIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void testCitizenRegistrationAndAuthenticationFlow() throws Exception {
        String testEmail = "testcitizen_" + System.currentTimeMillis() + "@civicpulse.com";
        String rawPassword = "securePass123";

        // 1. Register new citizen
        String registerJson = """
            {
                "fullName": "Priya Sharma",
                "email": "%s",
                "phone": "9876543210",
                "password": "%s",
                "confirmPassword": "%s",
                "address": "42, Brigade Road, Bangalore"
            }
            """.formatted(testEmail, rawPassword, rawPassword);

        mockMvc.perform(post("/api/citizen/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.citizenId").exists());

        // 2. Verify BCrypt password hashing in database
        Citizen citizen = citizenRepository.findByEmail(testEmail).orElse(null);
        assertNotNull(citizen, "Citizen should be saved in DB");
        assertNotEquals(rawPassword, citizen.getPassword(), "Raw password must not be stored in plaintext");
        assertTrue(passwordEncoder.matches(rawPassword, citizen.getPassword()), "Encoded password must match via BCrypt");

        // 3. Attempt duplicate email registration -> expect 400 Bad Request
        mockMvc.perform(post("/api/citizen/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // 4. Test login with wrong password -> expect 401 Unauthorized
        String wrongLoginJson = """
            {
                "email": "%s",
                "password": "wrongPassword"
            }
            """.formatted(testEmail);

        mockMvc.perform(post("/api/citizen/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongLoginJson))
                .andExpect(status().isUnauthorized());

        // 5. Test login with correct password -> expect 200 OK
        String validLoginJson = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(testEmail, rawPassword);

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/citizen/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validLoginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getRequest().getSession();

        assertNotNull(session, "Session should be established");
        assertEquals(testEmail, session.getAttribute("CITIZEN_EMAIL"));

        // 6. Test GET /api/citizen/auth/me with session
        mockMvc.perform(get("/api/citizen/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.fullName").value("Priya Sharma"));

        // 7. Test profile update
        String updateProfileJson = """
            {
                "fullName": "Priya S. Rao",
                "phone": "9999988888",
                "address": "100ft Road, Indiranagar"
            }
            """;

        mockMvc.perform(post("/api/citizen/profile").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateProfileJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Citizen updated = citizenRepository.findByEmail(testEmail).orElseThrow();
        assertEquals("Priya S. Rao", updated.getFullName());
        assertEquals("9999988888", updated.getPhone());
    }
}
