package com.civicpulse.controller;

import com.civicpulse.dto.ComplaintResponse;
import com.civicpulse.model.Complaint;
import com.civicpulse.model.TimelineEvent;
import com.civicpulse.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintApiController {

    @Autowired
    private ComplaintService complaintService;

    // 1. TRACK COMPLAINT
    @GetMapping("/{id}")
    public ResponseEntity<?> getComplaint(@PathVariable String id) {
        Complaint complaint = complaintService.getComplaint(id);
        if (complaint == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Complaint with ID " + id + " not found."));
        }
        
        ComplaintResponse response = new ComplaintResponse(
                complaint.getId(),
                complaint.getTitle(),
                complaint.getDescription(),
                complaint.getCategory(),
                complaint.getStatus(),
                complaint.getSubmittedDate(),
                complaint.getTimeline(),
                complaint.getImagePath()
        );
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

        // Add timeline event
        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd Aug yyyy, hh:mm a"));
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

        // Add timeline event
        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd Aug yyyy, hh:mm a"));
        complaint.getTimeline().add(new TimelineEvent(
                "Complaint Reopened",
                formattedDate,
                "Reason: " + (reason != null ? reason : "No reason provided."),
                "active"
        ));

        complaintService.saveComplaint(complaint);
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

            // 1. Get root directory of the application
            String rootPath = System.getProperty("user.dir");
            java.nio.file.Path uploadsDirPath = java.nio.file.Paths.get(rootPath, "uploads");

            // 2. Automatically create directories using Files.createDirectories
            java.nio.file.Files.createDirectories(uploadsDirPath);

            // 3. Resolve absolute path
            java.nio.file.Path targetFilePath = uploadsDirPath.resolve(fileName).toAbsolutePath();

            // 4. Save using Files.copy from input stream
            java.nio.file.Files.copy(file.getInputStream(), targetFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Set dynamic path accessible via /uploads/fileName
            String imagePath = "/uploads/" + fileName;
            complaint.setImagePath(imagePath);

            // Add timeline event
            String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd Aug yyyy, hh:mm a"));
            complaint.getTimeline().add(new TimelineEvent(
                    "Supporting Image Uploaded",
                    formattedDate,
                    "Image uploaded: " + originalFileName,
                    "completed"
            ));

            complaintService.saveComplaint(complaint);
            return ResponseEntity.ok(Map.of("success", true, "message", "Image uploaded successfully.", "imagePath", imagePath));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }
}
