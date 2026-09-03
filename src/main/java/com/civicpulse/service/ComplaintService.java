package com.civicpulse.service;

import com.civicpulse.dto.AttachmentDTO;
import com.civicpulse.dto.ComplaintDetailsDTO;
import com.civicpulse.model.Complaint;
import com.civicpulse.model.ComplaintAttachment;
import com.civicpulse.model.Officer;
import com.civicpulse.model.TimelineEvent;
import com.civicpulse.repository.ComplaintRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    public ComplaintService(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    /* =========================================================
       CITIZEN MODULE METHODS
    ========================================================= */

    @Transactional(readOnly = true)
    public Complaint getComplaint(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }

        // Try exact match on complaintNumber (e.g. CMP202600124)
        Optional<Complaint> byNumber = complaintRepository.findByComplaintNumber(id.trim());
        if (byNumber.isPresent()) {
            return byNumber.get();
        }

        // Try parsing numeric ID (e.g. 1, 2)
        try {
            Long numericId = Long.parseLong(id.trim());
            return complaintRepository.findById(numericId).orElse(null);
        } catch (NumberFormatException ignored) {
            // Not numeric
        }

        return null;
    }

    @Transactional
    public Complaint saveComplaint(Complaint complaint) {
        return complaintRepository.save(complaint);
    }

    @Transactional
    public Complaint createComplaint(String title, String description, String category, String location, String citizenName) {
        long count = complaintRepository.count() + 1;
        String complaintNumber = String.format("CMP2026%05d", count);

        Complaint complaint = new Complaint();
        complaint.setComplaintNumber(complaintNumber);
        complaint.setTitle(title);
        complaint.setDescription(description);
        complaint.setCategory(category != null ? category : "General");
        complaint.setLocation(location);
        complaint.setCitizenName(citizenName != null ? citizenName : "Citizen");
        complaint.setStatus("Pending");
        complaint.setPriority("Medium");
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());

        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        complaint.setSubmittedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        complaint.getTimeline().add(new TimelineEvent(
                "Complaint Submitted",
                formattedDate,
                "Your complaint has been successfully registered in CivicPulse.",
                "completed"
        ));
        complaint.getTimeline().add(new TimelineEvent(
                "Under Review",
                "Pending",
                "The complaint will be reviewed by the concerned department.",
                "active"
        ));

        return complaintRepository.save(complaint);
    }

    /* =========================================================
       ADMIN MODULE METHODS
    ========================================================= */

    @Transactional(readOnly = true)
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Complaint getComplaintById(Long id) {
        if (id == null) {
            return null;
        }
        return complaintRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteComplaint(Long id) {
        if (id != null) {
            complaintRepository.deleteById(id);
        }
    }

    @Transactional(readOnly = true)
    public long getTotalComplaints() {
        return complaintRepository.count();
    }

    @Transactional(readOnly = true)
    public long getPendingComplaints() {
        return countByStatus("Pending");
    }

    @Transactional(readOnly = true)
    public long getUnderReviewComplaints() {
        return complaintRepository.findAll()
                .stream()
                .filter(c -> c.getStatus() != null &&
                        (c.getStatus().equalsIgnoreCase("Under Review") ||
                         c.getStatus().equalsIgnoreCase("In Progress") ||
                         c.getStatus().equalsIgnoreCase("Assigned to Officer")))
                .count();
    }

    @Transactional(readOnly = true)
    public long getResolvedComplaints() {
        return countByStatus("Resolved");
    }

    private long countByStatus(String status) {
        return complaintRepository.findAll()
                .stream()
                .filter(c -> c.getStatus() != null && c.getStatus().equalsIgnoreCase(status))
                .count();
    }

    @Transactional(readOnly = true)
    public List<Complaint> getRecentComplaints() {
        return complaintRepository.findAll()
                .stream()
                .sorted((c1, c2) -> Long.compare(c2.getId() != null ? c2.getId() : 0, c1.getId() != null ? c1.getId() : 0))
                .limit(5)
                .collect(Collectors.toList());
    }

    /* =========================================================
       OFFICER MODULE METHODS
    ========================================================= */

    @Transactional(readOnly = true)
    public ComplaintDetailsDTO getComplaintDetailsById(String id) {
        Complaint complaint = getComplaint(id);
        if (complaint == null) {
            return null;
        }
        return mapToDTO(complaint);
    }

    @Transactional(readOnly = true)
    public List<ComplaintDetailsDTO> getAllComplaintDetails() {
        return complaintRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsForOfficer(Officer officer) {
        return complaintRepository.findByAssignedOfficer(officer);
    }

    @Transactional(readOnly = true)
    public Optional<Complaint> findById(Long id) {
        return complaintRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Complaint> findByComplaintNumber(String complaintNumber) {
        return complaintRepository.findByComplaintNumber(complaintNumber);
    }

    @Transactional
    public Complaint save(Complaint complaint) {
        return complaintRepository.save(complaint);
    }

    @Transactional
    public Complaint update(Complaint complaint) {
        return complaintRepository.save(complaint);
    }

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByStatus(Officer officer, String status) {
        return complaintRepository.findByAssignedOfficerAndStatus(officer, status);
    }

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByPriority(Officer officer, String priority) {
        return complaintRepository.findByAssignedOfficerAndPriority(officer, priority);
    }

    /* =========================================================
       DTO MAPPERS
    ========================================================= */

    private ComplaintDetailsDTO mapToDTO(Complaint complaint) {
        ComplaintDetailsDTO dto = new ComplaintDetailsDTO();

        dto.setId(complaint.getTrackingId());
        dto.setTitle(complaint.getTitle() != null ? complaint.getTitle() : "Untitled Complaint");
        dto.setDescription(complaint.getDescription());
        dto.setLocation(complaint.getLocation());
        dto.setDepartment(complaint.getDepartment());
        dto.setCategory(complaint.getCategory());
        dto.setPriority(complaint.getPriority() != null ? complaint.getPriority() : "Medium");
        dto.setStatus(complaint.getStatus() != null ? complaint.getStatus() : "Pending");

        dto.setCreatedAt(complaint.getCreatedAt());
        dto.setAssignedAt(complaint.getAssignedAt());
        dto.setUpdatedAt(complaint.getUpdatedAt());
        dto.setResolvedAt(complaint.getResolvedAt());

        if (complaint.getAttachments() != null && !complaint.getAttachments().isEmpty()) {
            List<AttachmentDTO> attachmentDTOs = complaint.getAttachments().stream()
                    .map(this::mapAttachmentToDTO)
                    .collect(Collectors.toList());
            dto.setAttachments(attachmentDTOs);
        } else {
            dto.setAttachments(Collections.emptyList());
        }

        return dto;
    }

    private AttachmentDTO mapAttachmentToDTO(ComplaintAttachment attachment) {
        AttachmentDTO dto = new AttachmentDTO();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileType(attachment.getFileType());
        dto.setFileSize(attachment.getFileSize());
        dto.setUploadedAt(attachment.getUploadedAt());

        if (attachment.getStoredFileName() != null) {
            dto.setFileUrl("/uploads/" + attachment.getStoredFileName());
        } else if (attachment.getFilePath() != null) {
            dto.setFileUrl(attachment.getFilePath());
        } else {
            dto.setFileUrl("#");
        }

        return dto;
    }
}
