package com.civicpulse.service;

import com.civicpulse.model.Complaint;
import com.civicpulse.repository.ComplaintRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DuplicateDetectionServiceTest {

    private ComplaintRepository complaintRepository;
    private DepartmentRoutingService departmentRoutingService;
    private DuplicateDetectionService duplicateDetectionService;

    @BeforeEach
    void setUp() {
        complaintRepository = Mockito.mock(ComplaintRepository.class);
        departmentRoutingService = new DepartmentRoutingService();
        duplicateDetectionService = new DuplicateDetectionService(complaintRepository, departmentRoutingService);
    }

    @Test
    void testDetectDuplicateWhenActiveComplaintMatches() {
        Complaint existing = new Complaint();
        existing.setComplaintNumber("CP-2026-9999");
        existing.setCategory("Road Damage");
        existing.setDepartment("Roads / Public Works Department");
        existing.setLocation("MG Road Metro Station");
        existing.setTitle("Huge Pothole");
        existing.setDescription("Massive pothole near entrance causing severe traffic jams");
        existing.setStatus("Pending");

        when(complaintRepository.findAll()).thenReturn(List.of(existing));

        DuplicateDetectionService.DuplicateCheckResult result = duplicateDetectionService.checkDuplicate(
                "Road Damage",
                "Pothole issue",
                "Severe pothole causing traffic near metro station entrance",
                "MG Road Metro Station"
        );

        assertTrue(result.isDuplicate());
        assertEquals("CP-2026-9999", result.getExistingComplaintNumber());
    }

    @Test
    void testDoNotFlagWhenComplaintIsResolved() {
        Complaint resolved = new Complaint();
        resolved.setComplaintNumber("CP-2026-8888");
        resolved.setCategory("Road Damage");
        resolved.setDepartment("Roads & Traffic");
        resolved.setLocation("MG Road Metro Station");
        resolved.setTitle("Pothole fixed");
        resolved.setDescription("Pothole near metro station entrance");
        resolved.setStatus("Resolved");

        when(complaintRepository.findAll()).thenReturn(List.of(resolved));

        DuplicateDetectionService.DuplicateCheckResult result = duplicateDetectionService.checkDuplicate(
                "Road Damage",
                "Pothole issue",
                "Pothole near metro station entrance",
                "MG Road Metro Station"
        );

        assertFalse(result.isDuplicate());
    }

    @Test
    void testDoNotFlagWhenLocationIsDifferent() {
        Complaint existing = new Complaint();
        existing.setComplaintNumber("CP-2026-7777");
        existing.setCategory("Road Damage");
        existing.setDepartment("Roads / Public Works Department");
        existing.setLocation("Indiranagar 100ft Road");
        existing.setTitle("Pothole");
        existing.setDescription("Pothole near signals");
        existing.setStatus("In Progress");

        when(complaintRepository.findAll()).thenReturn(List.of(existing));

        DuplicateDetectionService.DuplicateCheckResult result = duplicateDetectionService.checkDuplicate(
                "Road Damage",
                "Pothole",
                "Pothole near signals",
                "Whitefield Main Road"
        );

        assertFalse(result.isDuplicate());
    }

    @Test
    void testCalculateSimilarity() {
        double sim = duplicateDetectionService.calculateSimilarity(
                "Broken pipeline leaking drinking water",
                "Water pipeline leak spilling water"
        );
        assertTrue(sim > 0.3);
    }
}
