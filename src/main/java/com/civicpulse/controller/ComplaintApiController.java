package com.civicpulse.controller;

import com.civicpulse.dto.ComplaintResponse;
import com.civicpulse.model.Complaint;
import com.civicpulse.model.TimelineEvent;
import com.civicpulse.service.ComplaintService;
import com.civicpulse.service.DuplicateDetectionService;
import com.civicpulse.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintApiController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private DuplicateDetectionService duplicateDetectionService;

    @Autowired
    private NotificationService notificationService;

    private static final DateTimeFormatter TIMELINE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    // 0. SUBMIT COMPLAINT (WITH DUPLICATE DETECTION & DYNAMIC TIMESTAMPS)
    @PostMapping
    public ResponseEntity<?> submitComplaint(@RequestBody Map<String, Object> body, HttpSession session) {
        String title = (String) body.get("title");
        String description = (String) body.get("description");
        String category = (String) body.get("category");
        String location = (String) body.get("location");

        // Use session email/name if available, otherwise fall back to payload
        String sessionEmail = (String) session.getAttribute("CITIZEN_EMAIL");
        String sessionName = (String) session.getAttribute("CITIZEN_NAME");

        String citizenName = sessionName != null ? sessionName : (String) body.getOrDefault("citizenName", "Citizen");
        String citizenEmail = sessionEmail != null ? sessionEmail : (String) body.getOrDefault("citizenEmail", "citizen@civicpulse.com");
        String citizenContact = (String) body.getOrDefault("citizenContact", "");

        Boolean forceSubmit = (Boolean) body.getOrDefault("forceSubmit", false);

        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title and description are required."));
        }
        if (location == null || location.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Location is required."));
        }

        // Check for duplicate / similar complaint unless forced
        if (Boolean.FALSE.equals(forceSubmit)) {
            DuplicateDetectionService.DuplicateCheckResult dupResult =
                    duplicateDetectionService.checkDuplicate(category, title, description, location);

            if (dupResult.isDuplicate()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "duplicate", true,
                        "existingComplaintId", dupResult.getExistingComplaintNumber(),
                        "existingStatus", dupResult.getExistingStatus(),
                        "existingTitle", dupResult.getExistingTitle() != null ? dupResult.getExistingTitle() : "",
                        "existingCategory", dupResult.getExistingCategory() != null ? dupResult.getExistingCategory() : "",
                        "existingLocation", dupResult.getExistingLocation() != null ? dupResult.getExistingLocation() : "",
                        "message", dupResult.getMessage()
                ));
            }
        }

        Complaint complaint = complaintService.createComplaint(
                title, description, category, location, citizenName, citizenEmail, citizenContact
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Complaint registered successfully.",
                "complaintId", complaint.getTrackingId(),
                "id", complaint.getId(),
                "department", complaint.getDepartment() != null ? complaint.getDepartment() : ""
        ));
    }

    // 1. TRACK COMPLAINT
    @GetMapping("/{id}")
    public ResponseEntity<?> getComplaint(@PathVariable String id) {
        Complaint complaint = complaintService.getComplaint(id);
        if (complaint == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Complaint with ID " + id + " not found."));
        }

        ComplaintResponse response = new ComplaintResponse(
                complaint.getTrackingId(),
                complaint.getTitle(),
                complaint.getDescription(),
                complaint.getCategory(),
                complaint.getStatus(),
                complaint.getSubmittedDate(),
                complaint.getTimeline(),
                complaint.getImagePath()
        );

        response.setLocation(complaint.getLocation());
        response.setDepartment(complaint.getDepartment());
        if (complaint.getAssignedOfficer() != null) {
            response.setAssignedOfficer(complaint.getAssignedOfficer().getFullName());
            response.setAssignedOfficerDesignation(complaint.getAssignedOfficer().getDesignation());
        }
        response.setCitizenName(complaint.getCitizenName());
        response.setResolution(complaint.getResolution());
        response.setRemarks(complaint.getRemarks());

        if (complaint.getCreatedAt() != null) {
            response.setCreatedAt(complaint.getCreatedAt().format(TIMELINE_FORMATTER));
        }
        if (complaint.getUpdatedAt() != null) {
            response.setUpdatedAt(complaint.getUpdatedAt().format(TIMELINE_FORMATTER));
        }
        if (complaint.getAssignedAt() != null) {
            response.setAssignedAt(complaint.getAssignedAt().format(TIMELINE_FORMATTER));
        }
        if (complaint.getInProgressAt() != null) {
            response.setInProgressAt(complaint.getInProgressAt().format(TIMELINE_FORMATTER));
        }
        if (complaint.getResolvedAt() != null) {
            response.setResolvedAt(complaint.getResolvedAt().format(TIMELINE_FORMATTER));
        }

        return ResponseEntity.ok(response);
    }

    // 2. FEEDBACK
    @PostMapping("/{id}/feedback")
    public ResponseEntity<?> submitFeedback(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Complaint complaint = complaintService.getComplaint(id);
        if (complaint == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Complaint not found."));
        }

        Integer rating = null;
        if (body.containsKey("rating")) {
            Object r = body.get("rating");
            if (r instanceof Number) {
                rating = ((Number) r).intValue();
            } else if (r instanceof String) {
                rating = Integer.parseInt((String) r);
            }
        }
        String comment = (String) body.get("comment");

        complaint.setFeedbackRating(rating);
        complaint.setFeedbackComment(comment);

        // Add timeline event with dynamic date
        String formattedDate = LocalDateTime.now().format(TIMELINE_FORMATTER);
        complaint.getTimeline().add(new TimelineEvent(
                "Feedback Submitted",
                formattedDate,
                "Rating: " + rating + "/5 stars. Comment: " + (comment != null ? comment : ""),
                "completed"
        ));

        complaintService.saveComplaint(complaint);
        return ResponseEntity.ok(Map.of("success", true, "message", "Feedback submitted successfully."));
    }

    // 3. REOPEN REQUEST
    @PostMapping("/{id}/reopen")
    public ResponseEntity<?> reopenComplaint(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Complaint complaint = complaintService.getComplaint(id);
        if (complaint == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Complaint not found."));
        }

        String reason = (String) body.get("reason");
        complaint.setReopenReason(reason);
        complaint.setStatus("Reopened");

        String formattedDate = LocalDateTime.now().format(TIMELINE_FORMATTER);
        complaint.getTimeline().add(new TimelineEvent(
                "Complaint Reopened",
                formattedDate,
                "Reason: " + (reason != null ? reason : "No reason provided."),
                "active"
        ));

        complaintService.saveComplaint(complaint);

        // Notify citizen
        notificationService.createNotification(
                complaint.getCitizenEmail(),
                "Complaint Reopened",
                "Your complaint " + complaint.getTrackingId() + " has been reopened for further investigation.",
                complaint.getTrackingId(),
                "STATUS_CHANGE"
        );

        return ResponseEntity.ok(Map.of("success", true, "message", "Complaint reopened successfully."));
    }

    // 4. IMAGE UPLOAD
    @PostMapping("/{id}/image")
    public ResponseEntity<?> uploadImage(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        Complaint complaint = complaintService.getComplaint(id);
        if (complaint == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Complaint not found."));
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded. Please upload a valid image."));
        }

        try {
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = id + "_" + System.currentTimeMillis() + extension;

            String rootPath = System.getProperty("user.dir");
            java.nio.file.Path uploadsDirPath = java.nio.file.Paths.get(rootPath, "uploads");
            java.nio.file.Files.createDirectories(uploadsDirPath);

            java.nio.file.Path targetFilePath = uploadsDirPath.resolve(fileName).toAbsolutePath();
            java.nio.file.Files.copy(file.getInputStream(), targetFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String imagePath = "/uploads/" + fileName;
            complaint.setImagePath(imagePath);

            String formattedDate = LocalDateTime.now().format(TIMELINE_FORMATTER);
            complaint.getTimeline().add(new TimelineEvent(
                    "Supporting Image Uploaded",
                    formattedDate,
                    "Image uploaded: " + (originalFileName != null ? originalFileName : fileName),
                    "completed"
            ));

            complaintService.saveComplaint(complaint);
            return ResponseEntity.ok(Map.of("success", true, "message", "Image uploaded successfully.", "imagePath", imagePath));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }

    // 5. NOTIFICATIONS READ-ALL
    @PostMapping("/notifications/read-all")
    public ResponseEntity<?> markAllNotificationsRead(HttpSession session) {
        String email = (String) session.getAttribute("CITIZEN_EMAIL");
        if (email != null) {
            notificationService.markAllAsRead(email);
        }
        return ResponseEntity.ok(Map.of("success", true));
    }
}
