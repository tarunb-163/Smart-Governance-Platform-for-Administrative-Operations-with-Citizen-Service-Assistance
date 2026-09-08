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

    @Test
    void testRegistrationWithUsernameAndFlexibleLoginFlow() throws Exception {
        String testUser = "rajesh_" + System.currentTimeMillis();
        String testEmail = testUser + "@civicpulse.com";
        String rawPassword = "mypassword123";

        // 1. Register with custom username
        String registerJson = """
            {
                "fullName": "Rajesh Kumar",
                "username": "%s",
                "email": "%s",
                "phone": "9876500000",
                "password": "%s",
                "confirmPassword": "%s",
                "address": "12, Park Street, Kolkata"
            }
            """.formatted(testUser, testEmail, rawPassword, rawPassword);

        String responseBody = mockMvc.perform(post("/api/citizen/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.citizenId").exists())
                .andReturn().getResponse().getContentAsString();

        Citizen citizen = citizenRepository.findByUsernameIgnoreCase(testUser).orElseThrow();
        String citizenId = citizen.getCitizenId();
        assertNotNull(citizen.getLastLogin(), "Initial registration should set lastLogin");

        // 2. Duplicate username registration -> expect 400
        String dupUserJson = """
            {
                "fullName": "Another Rajesh",
                "username": "%s",
                "email": "diff_%s@civicpulse.com",
                "phone": "9876500001",
                "password": "%s",
                "confirmPassword": "%s",
                "address": "Kolkata"
            }
            """.formatted(testUser, System.currentTimeMillis(), rawPassword, rawPassword);

        mockMvc.perform(post("/api/citizen/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dupUserJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("An account with this username already exists."));

        // 3. Login with username
        String loginByUsernameJson = """
            {
                "identifier": "%s",
                "password": "%s"
            }
            """.formatted(testUser, rawPassword);

        mockMvc.perform(post("/api/citizen/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginByUsernameJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 4. Login with mixed-case email
        String loginByMixedEmailJson = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(testEmail.toUpperCase(), rawPassword);

        mockMvc.perform(post("/api/citizen/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginByMixedEmailJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Login with lowercase citizen ID
        String loginByCitizenIdJson = """
            {
                "identifier": "%s",
                "password": "%s"
            }
            """.formatted(citizenId.toLowerCase(), rawPassword);

        mockMvc.perform(post("/api/citizen/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginByCitizenIdJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 6. Login with unknown user -> 401
        String loginUnknownJson = """
            {
                "identifier": "nonexistent_user_999",
                "password": "%s"
            }
            """.formatted(rawPassword);

        mockMvc.perform(post("/api/citizen/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginUnknownJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email/username or password."));
    }

    @Test
    void testLogoutDoesNotDeleteUserAndAllowsSuccessfulReLogin() throws Exception {
        String testEmail = "logout_test_" + System.currentTimeMillis() + "@civicpulse.com";
        String rawPassword = "password123";

        // 1. Register new citizen
        String registerJson = """
            {
                "fullName": "Kiran Rao",
                "email": "%s",
                "phone": "9811122233",
                "password": "%s",
                "confirmPassword": "%s",
                "address": "Hyderabad"
            }
            """.formatted(testEmail, rawPassword, rawPassword);

        mockMvc.perform(post("/api/citizen/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        // 2. Perform logout
        mockMvc.perform(get("/citizen/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/citizen/login?logout"));

        // 3. Verify user still exists in database
        Citizen citizen = citizenRepository.findByEmail(testEmail).orElse(null);
        assertNotNull(citizen, "Logout MUST NOT delete the registered citizen from storage");

        // 4. Log in again with the exact same credentials
        String loginJson = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(testEmail, rawPassword);

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/citizen/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getRequest().getSession();

        assertNotNull(session);
        assertEquals(testEmail, session.getAttribute("CITIZEN_EMAIL"));

        // 5. Access dashboard with the session
        mockMvc.perform(get("/citizen/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("citizen/dashboard"))
                .andExpect(model().attributeExists("citizen"));
    }

    @Test
    void testAdminCitizenManagementIntegration() throws Exception {
        // GET /admin/citizens should render successfully with live citizen data
        mockMvc.perform(get("/admin/citizens"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/citizens"))
                .andExpect(model().attributeExists("citizens"))
                .andExpect(model().attributeExists("totalCitizens"))
                .andExpect(model().attributeExists("activeCitizens"))
                .andExpect(model().attributeExists("inactiveCitizens"));

        // Test status toggle endpoint
        Citizen citizen = citizenRepository.findAll().stream().findFirst().orElseThrow();
        boolean initialStatus = citizen.isActive();

        mockMvc.perform(post("/admin/citizens/" + citizen.getId() + "/toggle-status"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/citizens"));

        Citizen reloaded = citizenRepository.findById(citizen.getId()).orElseThrow();
        assertEquals(!initialStatus, reloaded.isActive(), "Citizen active status should be toggled");

        // Toggle back
        mockMvc.perform(post("/admin/citizens/" + citizen.getId() + "/toggle-status"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testSpringSecurityUnifiedFormLoginForCitizen() throws Exception {
        String testUser = "unified_" + System.currentTimeMillis();
        String testEmail = testUser + "@civicpulse.com";
        String rawPassword = "securePassword123";

        // Register citizen
        String registerJson = """
            {
                "fullName": "Unified Citizen",
                "username": "%s",
                "email": "%s",
                "password": "%s",
                "confirmPassword": "%s"
            }
            """.formatted(testUser, testEmail, rawPassword, rawPassword);

        mockMvc.perform(post("/api/citizen/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        // Perform Spring Security form login POST /login with username
        mockMvc.perform(post("/login")
                        .param("username", testUser)
                        .param("password", rawPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/citizen/dashboard"));

        // Perform Spring Security form login POST /login with email
        mockMvc.perform(post("/login")
                        .param("username", testEmail)
                        .param("password", rawPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/citizen/dashboard"));
    }
}
