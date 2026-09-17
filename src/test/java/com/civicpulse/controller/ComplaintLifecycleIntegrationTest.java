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
import org.springframework.mock.web.MockMultipartFile;
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

        // 4b. Verify feedback cannot be submitted while complaint is still In Progress
        mockMvc.perform(post("/api/complaints/" + complaintId + "/feedback")
                        .session(citizenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"rating": 4, "comment": "Too early"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // 4c. Verify complaint cannot be reopened while still In Progress
        mockMvc.perform(post("/api/complaints/" + complaintId + "/reopen")
                        .session(citizenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"reason": "Cannot reopen active issue"}
                        """))
                .andExpect(status().isBadRequest());

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

        // 5b. Officer checks complaint details: Verify citizen information is populated
        mockMvc.perform(get("/api/officer/complaints/" + complaintId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(complaintId))
                .andExpect(jsonPath("$.citizenName").value("Rahul Verma"))
                .andExpect(jsonPath("$.citizenContact").value("9876543210"))
                .andExpect(jsonPath("$.citizenEmail").value(citizenEmail));

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

        // 8b. Prevent duplicate feedback submission
        mockMvc.perform(post("/api/complaints/" + complaintId + "/feedback")
                        .session(citizenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Feedback has already been submitted for this complaint."));

        // 8c. Verify tracking endpoint returns stored feedback
        mockMvc.perform(get("/api/complaints/" + complaintId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedbackRating").value(5))
                .andExpect(jsonPath("$.feedbackComment").value("Thank you for fixing it quickly!"));

        // 9a. Citizen Reopen test: Reopen without reason should fail
        mockMvc.perform(post("/api/complaints/" + complaintId + "/reopen")
                        .session(citizenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"reason": "   "}
                        """))
                .andExpect(status().isBadRequest());

        // 9b. Citizen Reopen test: Valid reason succeeds
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

        // 9c. Verify reopened complaint state and that reopen cannot be called again
        mockMvc.perform(post("/api/complaints/" + complaintId + "/reopen")
                        .session(citizenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reopenJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only complaints that are currently resolved or closed can be reopened."));

        Complaint reopened = complaintRepository.findByComplaintNumber(complaintId).orElseThrow();
        assertEquals("Reopened", reopened.getStatus());
        assertEquals("Recent rain washed away the loose patch", reopened.getReopenReason());

        // Verify tracking endpoint returns reopenReason
        mockMvc.perform(get("/api/complaints/" + complaintId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Reopened"))
                .andExpect(jsonPath("$.reopenReason").value("Recent rain washed away the loose patch"));
    }

    @Test
    @WithMockUser(username = "officer1", roles = {"OFFICER"})
    void testComplaintImageEvidenceUploadAndRetrieval() throws Exception {
        String citizenEmail = "evidence_test_" + System.currentTimeMillis() + "@civicpulse.com";
        MockHttpSession citizenSession = new MockHttpSession();
        citizenSession.setAttribute("CITIZEN_EMAIL", citizenEmail);
        citizenSession.setAttribute("CITIZEN_NAME", "Priya Sharma");

        // 1. Submit a complaint
        String submitJson = """
            {
                "title": "Broken water supply main",
                "description": "Clean drinking water leaking on road opposite gate 2",
                "category": "Water Supply",
                "location": "Near Gate 2, Sector 5",
                "citizenContact": "9876543219"
            }
            """;

        MvcResult submitResult = mockMvc.perform(post("/api/complaints")
                        .session(citizenSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitJson))
                .andExpect(status().isCreated())
                .andReturn();

        String complaintId = JsonPath.read(submitResult.getResponse().getContentAsString(), "$.complaintId");
        assertNotNull(complaintId);

        // 2. Upload image evidence
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "leakage_evidence.jpg",
                "image/jpeg",
                "dummy image content bytes for test".getBytes()
        );

        mockMvc.perform(multipart("/api/complaints/" + complaintId + "/image")
                        .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.imagePath").exists());

        // 3. Verify Complaint entity has imagePath and attachment in DB
        Complaint complaint = complaintRepository.findByComplaintNumber(complaintId).orElseThrow();
        assertNotNull(complaint.getImagePath());
        assertTrue(complaint.getImagePath().startsWith("/uploads/"));
        assertNotNull(complaint.getAttachments());
        assertFalse(complaint.getAttachments().isEmpty());
        assertEquals("leakage_evidence.jpg", complaint.getAttachments().get(0).getFileName());

        // 4. Verify Citizen tracking API returns imagePath and attachments
        mockMvc.perform(get("/api/complaints/" + complaintId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagePath").value(complaint.getImagePath()))
                .andExpect(jsonPath("$.attachments").isArray())
                .andExpect(jsonPath("$.attachments[0].fileName").value("leakage_evidence.jpg"))
                .andExpect(jsonPath("$.attachments[0].fileUrl").value(complaint.getImagePath()));

        // 5. Verify Officer complaint details API returns imagePath and attachments
        mockMvc.perform(get("/api/officer/complaints/" + complaintId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagePath").value(complaint.getImagePath()))
                .andExpect(jsonPath("$.attachments").isArray())
                .andExpect(jsonPath("$.attachments[0].fileName").value("leakage_evidence.jpg"))
                .andExpect(jsonPath("$.attachments[0].fileUrl").value(complaint.getImagePath()));

        // 6. Verify image can be retrieved via HTTP resource handler
        mockMvc.perform(get(complaint.getImagePath()))
                .andExpect(status().isOk());
    }
}
