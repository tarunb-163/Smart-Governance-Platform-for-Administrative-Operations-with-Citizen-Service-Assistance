package com.civicpulse.controller.api;

import com.civicpulse.dto.ComplaintDetailsDTO;
import com.civicpulse.model.Complaint;
import com.civicpulse.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/officer/complaints")
public class OfficerComplaintApiController {

    private final ComplaintService complaintService;

    @Autowired
    public OfficerComplaintApiController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    /**
     * GET /api/officer/complaints
     * Retrieves all complaints formatted for officer overview.
     */
    @GetMapping
    public ResponseEntity<List<ComplaintDetailsDTO>> getAllComplaints() {
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
     * Allows officer to update complaint status and remarks.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateComplaint(@PathVariable("id") String id, @RequestBody Map<String, Object> body) {
        Complaint complaint = complaintService.getComplaint(id);
        if (complaint == null) {
            return ResponseEntity.notFound().build();
        }

        if (body.containsKey("status")) {
            complaint.setStatus((String) body.get("status"));
        }
        if (body.containsKey("remarks")) {
            complaint.setRemarks((String) body.get("remarks"));
        }
        if (body.containsKey("priority")) {
            complaint.setPriority((String) body.get("priority"));
        }

        complaintService.save(complaint);
        return ResponseEntity.ok(Map.of("success", true, "message", "Complaint updated successfully."));
    }
}
