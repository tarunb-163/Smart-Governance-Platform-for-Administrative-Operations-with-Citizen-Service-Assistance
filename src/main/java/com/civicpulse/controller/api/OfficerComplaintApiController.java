package com.civicpulse.controller.api;

import com.civicpulse.dto.ComplaintDetailsDTO;
import com.civicpulse.model.Complaint;
import com.civicpulse.model.Officer;
import com.civicpulse.service.ComplaintService;
import com.civicpulse.service.OfficerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/officer/complaints")
public class OfficerComplaintApiController {

    private final ComplaintService complaintService;
    private final OfficerService officerService;

    @Autowired
    public OfficerComplaintApiController(ComplaintService complaintService, OfficerService officerService) {
        this.complaintService = complaintService;
        this.officerService = officerService;
    }

    /**
     * GET /api/officer/complaints
     * Retrieves all complaints formatted for officer overview.
     * If an officer is authenticated, returns complaints relevant to their department/assignment.
     */
    @GetMapping
    public ResponseEntity<List<ComplaintDetailsDTO>> getAllComplaints(Principal principal) {
        if (principal != null) {
            Optional<Officer> officerOpt = officerService.findByUsername(principal.getName());
            if (officerOpt.isPresent()) {
                return ResponseEntity.ok(complaintService.getComplaintDetailsForOfficer(officerOpt.get()));
            }
        }
        List<ComplaintDetailsDTO> list = complaintService.getAllComplaintDetails();
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/officer/complaints/{id}
     * Retrieves full complaint details for officer review.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ComplaintDetailsDTO> getComplaintById(@PathVariable("id") String id) {
        ComplaintDetailsDTO complaint = complaintService.getComplaintDetailsById(id);

        if (complaint == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(complaint);
    }

    /**
     * PUT /api/officer/complaints/{id}
     * Allows officer to update complaint status, priority, remarks, and resolution.
     * Updates timestamps, timeline events, and notifies citizen.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateComplaint(@PathVariable("id") String id,
                                             @RequestBody Map<String, Object> body,
                                             Principal principal) {
        Complaint complaint = complaintService.getComplaint(id);
        if (complaint == null) {
            return ResponseEntity.notFound().build();
        }

        String newStatus = (String) body.get("status");
        String remarks = (String) body.get("remarks");
        String resolution = (String) body.get("resolution");
        String priority = (String) body.get("priority");

        if (priority != null && !priority.trim().isEmpty()) {
            complaint.setPriority(priority.trim());
            complaintService.saveComplaint(complaint);
        }

        String officerName = principal != null ? principal.getName() : "Field Officer";

        if (newStatus != null && !newStatus.trim().isEmpty()) {
            complaintService.updateStatus(id, newStatus, remarks, resolution, officerName);
        } else if (remarks != null && !remarks.trim().isEmpty()) {
            complaint.setRemarks(remarks.trim());
            complaintService.saveComplaint(complaint);
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Complaint updated successfully."));
    }
}
