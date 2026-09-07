package com.civicpulse.controller;

import com.civicpulse.model.Citizen;
import com.civicpulse.model.Complaint;
import com.civicpulse.model.Notification;
import com.civicpulse.repository.CitizenRepository;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.repository.NotificationRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ComplaintLifecycleIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CitizenRepository citizenRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @WithMockUser(username = "officer1", roles = {"OFFICER"})
    void testFullComplaintLifecycleAndDuplicateDetection() throws Exception {
        String citizenEmail = "citizen_lifecycle_" + System.currentTimeMillis() + "@civicpulse.com";

        // Setup session for citizen
        MockHttpSession citizenSession = new MockHttpSession();
        citizenSession.setAttribute("CITIZEN_EMAIL", citizenEmail);
        citizenSession.setAttribute("CITIZEN_NAME", "Rahul Verma");

        // 1. Submit Complaint (Road Damage)
        String submitJson = """
            {
                "title": "Large pothole hazard",
                "description": "Deep dangerous pothole near main bus stop causing traffic jams and bike accidents",
                "category": "Road Damage",
                "location": "Main Bus Stop, MG Road",
                "citizenContact": "9876543210"
            }
            """;

        MvcResult submitResult = mockMvc.perform(post("/api/complaints")
                        .session(citizenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.complaintId").exists())
                .andExpect(jsonPath("$.department").value("Roads / Public Works Department"))
                .andReturn();

        String complaintId = JsonPath.read(submitResult.getResponse().getContentAsString(), "$.complaintId");
        assertNotNull(complaintId);
        assertTrue(complaintId.startsWith("CMP"));

        // 2. Duplicate Detection: Attempt to submit highly similar complaint at same location
        String duplicateJson = """
            {
                "title": "Road damage pothole",
                "description": "Deep dangerous pothole near main bus stop causing traffic problems",
                "category": "Road Damage",
                "location": "Main Bus Stop, MG Road",
                "citizenContact": "9123456780"
            }
            """;

        mockMvc.perform(post("/api/complaints")
                        .session(citizenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.duplicate").value(true))
                .andExpect(jsonPath("$.existingComplaintId").value(complaintId));

        // 3. Bypass duplicate check with forceSubmit: true
        String forceSubmitJson = """
            {
                "title": "Road damage pothole",
                "description": "Deep dangerous pothole near main bus stop causing traffic problems",
                "category": "Road Damage",
                "location": "Main Bus Stop, MG Road",
                "citizenContact": "9123456780",
                "forceSubmit": true
            }
            """;

        mockMvc.perform(post("/api/complaints")
                        .session(citizenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(forceSubmitJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // 4. Officer updates status to "In Progress"
        String updateToInProgress = """
            {
                "status": "In Progress",
                "remarks": "Road maintenance crew dispatched with asphalt mixer.",
                "priority": "High"
            }
            """;

        mockMvc.perform(put("/api/officer/complaints/" + complaintId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateToInProgress))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Citizen tracks complaint: Verify timeline and dynamic timestamps
        MvcResult trackResult = mockMvc.perform(get("/api/complaints/" + complaintId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingId").value(complaintId))
                .andExpect(jsonPath("$.status").value("In Progress"))
                .andExpect(jsonPath("$.department").value("Roads / Public Works Department"))
                .andExpect(jsonPath("$.inProgressAt").isNotEmpty())
                .andReturn();

        String trackResponse = trackResult.getResponse().getContentAsString();
        assertFalse(trackResponse.contains("Aug 2026"), "Should not contain hardcoded August date");

        // 6. Officer resolves complaint
        String updateToResolved = """
            {
                "status": "Resolved",
                "remarks": "Pothole filled and road surface restored.",
                "resolution": "Permanent cold mix asphalt patch applied and leveled with roller."
            }
            """;

        mockMvc.perform(put("/api/officer/complaints/" + complaintId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateToResolved))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 7. Verify Citizen Notification was created
        List<Notification> notifications = notificationRepository.findByCitizenEmailOrderByCreatedAtDesc(citizenEmail);
        assertFalse(notifications.isEmpty(), "Notification must be generated for citizen");
        assertTrue(notifications.stream().anyMatch(n -> n.getComplaintNumber().equals(complaintId) && n.getMessage().contains("Resolved")));

        // 8. Citizen submits 5-star feedback
        String feedbackJson = """
            {
                "rating": 5,
                "comment": "Thank you for fixing it quickly!"
            }
            """;

        mockMvc.perform(post("/api/complaints/" + complaintId + "/feedback")
                        .session(citizenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 9. Citizen Reopen test
        String reopenJson = """
            {
                "reason": "Recent rain washed away the loose patch"
            }
            """;

        mockMvc.perform(post("/api/complaints/" + complaintId + "/reopen")
                        .session(citizenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reopenJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Complaint reopened = complaintRepository.findByComplaintNumber(complaintId).orElseThrow();
        assertEquals("Reopened", reopened.getStatus());
        assertEquals("Recent rain washed away the loose patch", reopened.getReopenReason());
    }
}
