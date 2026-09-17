package com.civicpulse.controller;

import com.civicpulse.model.Notification;
import com.civicpulse.model.Officer;
import com.civicpulse.repository.NotificationRepository;
import com.civicpulse.repository.OfficerRepository;
import com.civicpulse.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class OfficerFeatureIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OfficerRepository officerRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String testOfficerUsername = "test_officer_" + System.currentTimeMillis();
    private final String initialPassword = "initialPass123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        // Create a test officer if not exists
        if (officerRepository.findByUsername(testOfficerUsername).isEmpty()) {
            Officer officer = new Officer();
            officer.setUsername(testOfficerUsername);
            officer.setPassword(passwordEncoder.encode(initialPassword));
            officer.setFullName("Test Officer");
            officer.setEmployeeId("EMP_TEST_" + System.currentTimeMillis());
            officer.setEmail(testOfficerUsername + "@civicpulse.com");
            officer.setRole("OFFICER");
            officer.setActive(true);
            officerRepository.save(officer);
        }
    }

    @Test
    @WithMockUser(username = "officer1", roles = {"OFFICER"})
    void testOfficerNotificationsViewAndMarkRead() throws Exception {
        // Create an unread test notification for officer1
        notificationService.createOfficerNotification(
                "officer1",
                "Integration Test Alert",
                "Checking officer notifications center functionality",
                "CMP202600999",
                "ASSIGNED"
        );

        // Verify page loads with notificationsList and unreadCount
        mockMvc.perform(get("/officer/notifications"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("notificationsList"))
                .andExpect(model().attributeExists("unreadCount"));

        // Mark all as read via form post
        mockMvc.perform(post("/officer/notifications/mark-all-read"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/officer/notifications"));

        // Verify unread count is 0
        long unreadAfter = notificationService.getUnreadCountForOfficer("officer1");
        assertEquals(0, unreadAfter);
    }

    @Test
    void testOfficerChangePasswordFlow() throws Exception {
        // 1. Wrong current password
        mockMvc.perform(post("/officer/change-password")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(testOfficerUsername).roles("OFFICER"))
                        .param("currentPassword", "wrongPass")
                        .param("newPassword", "newSecret123")
                        .param("confirmPassword", "newSecret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("passwordError"));

        // 2. Mismatching new password and confirm password
        mockMvc.perform(post("/officer/change-password")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(testOfficerUsername).roles("OFFICER"))
                        .param("currentPassword", initialPassword)
                        .param("newPassword", "newSecret123")
                        .param("confirmPassword", "differentPass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("passwordError"));

        // 3. Successful change password
        mockMvc.perform(post("/officer/change-password")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(testOfficerUsername).roles("OFFICER"))
                        .param("currentPassword", initialPassword)
                        .param("newPassword", "newSecret123")
                        .param("confirmPassword", "newSecret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("passwordSuccess"));

        // Verify in DB that new password matches
        Officer updatedOfficer = officerRepository.findByUsername(testOfficerUsername).orElseThrow();
        assertTrue(passwordEncoder.matches("newSecret123", updatedOfficer.getPassword()));

        // 4. Test API endpoint (used by modal)
        String modalApiJson = """
            {
                "currentPassword": "newSecret123",
                "newPassword": "modalUpdatedPass456",
                "confirmPassword": "modalUpdatedPass456"
            }
        """;

        mockMvc.perform(post("/api/officer/change-password")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(testOfficerUsername).roles("OFFICER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modalApiJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Officer modalUpdated = officerRepository.findByUsername(testOfficerUsername).orElseThrow();
        assertTrue(passwordEncoder.matches("modalUpdatedPass456", modalUpdated.getPassword()));
    }
}
