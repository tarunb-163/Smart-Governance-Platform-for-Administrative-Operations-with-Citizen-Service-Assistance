package com.civicpulse.service;

import com.civicpulse.dto.AttachmentDTO;
import com.civicpulse.dto.ComplaintDetailsDTO;
import com.civicpulse.model.Complaint;
import com.civicpulse.model.ComplaintAttachment;
import com.civicpulse.model.Officer;
import com.civicpulse.model.TimelineEvent;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.repository.OfficerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final OfficerRepository officerRepository;
    private final DepartmentRoutingService departmentRoutingService;
    private final NotificationService notificationService;

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public ComplaintService(ComplaintRepository complaintRepository,
                            OfficerRepository officerRepository,
                            DepartmentRoutingService departmentRoutingService,
                            NotificationService notificationService) {
        this.complaintRepository = complaintRepository;
        this.officerRepository = officerRepository;
        this.departmentRoutingService = departmentRoutingService;
        this.notificationService = notificationService;
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
    public Complaint createComplaint(String title, String description, String category, String location,
                                    String citizenName, String citizenEmail, String citizenContact) {

        long count = complaintRepository.count() + 1;
        String complaintNumber = String.format("CMP%d%05d", LocalDate.now().getYear(), count);
        while (complaintRepository.findByComplaintNumber(complaintNumber).isPresent()) {
            count++;
            complaintNumber = String.format("CMP%d%05d", LocalDate.now().getYear(), count);
        }

        // 1. Department Routing
        String department = departmentRoutingService.determineDepartment(category, title, description);

        // 2. Automatically find and assign department officer if available
        Officer assignedOfficer = findOfficerForDepartment(department);

        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(DATE_TIME_FORMATTER);
        String formattedDate = now.format(DATE_FORMATTER);

        Complaint complaint = new Complaint();
        complaint.setComplaintNumber(complaintNumber);
        complaint.setTitle(title != null ? title.trim() : "Civic Issue");
        complaint.setDescription(description != null ? description.trim() : "");
        complaint.setCategory(category != null ? category.trim() : "General");
        complaint.setLocation(location != null ? location.trim() : "");
        complaint.setCitizenName(citizenName != null ? citizenName.trim() : "Citizen");
        complaint.setCitizenEmail(citizenEmail != null ? citizenEmail.trim().toLowerCase() : "citizen@civicpulse.com");
        complaint.setCitizenContact(citizenContact != null ? citizenContact.trim() : "");
        complaint.setDepartment(department);
        complaint.setPriority("Medium");
        complaint.setCreatedAt(now);
        complaint.setUpdatedAt(now);
        complaint.setSubmittedDate(formattedDate);

        // 3. Build Dynamic Timeline
        complaint.getTimeline().add(new TimelineEvent(
                "Complaint Submitted",
                formattedDateTime,
                "Your complaint has been successfully registered in CivicPulse.",
                "completed"
        ));

        complaint.getTimeline().add(new TimelineEvent(
                "Assigned to Department",
                formattedDateTime,
                "Complaint automatically routed to " + department + ".",
                "completed"
        ));

        if (assignedOfficer != null) {
            complaint.setAssignedOfficer(assignedOfficer);
            complaint.setAssignedAt(now);
            complaint.setStatus("Assigned to Officer");

            complaint.getTimeline().add(new TimelineEvent(
                    "Officer Assigned",
                    formattedDateTime,
                    "Assigned to Officer: " + assignedOfficer.getFullName() + " (" + assignedOfficer.getDesignation() + ").",
                    "completed"
            ));

            complaint.getTimeline().add(new TimelineEvent(
                    "Under Investigation",
                    "Pending",
                    "Officer will inspect the reported issue on site.",
                    "active"
            ));
        } else {
            complaint.setStatus("Pending");
            complaint.getTimeline().add(new TimelineEvent(
                    "Under Review",
                    "Pending",
                    "The complaint is queued for review by " + department + ".",
                    "active"
            ));
        }

        complaint.getTimeline().add(new TimelineEvent(
                "Resolution Pending",
                "Pending",
                "Awaiting departmental action and completion.",
                "pending"
        ));

        complaint.getTimeline().add(new TimelineEvent(
                "Resolved",
                "Pending",
                "The complaint will be marked resolved after verification.",
                "pending"
        ));

        Complaint saved = complaintRepository.save(complaint);

        // 4. Send Notification to Citizen
        if (citizenEmail != null && !citizenEmail.trim().isEmpty()) {
            notificationService.createNotification(
                    citizenEmail,
                    "Complaint Registered",
                    "Your complaint " + complaintNumber + " has been registered and routed to " + department + ".",
                    complaintNumber,
                    "SUBMISSION"
            );
        }

        return saved;
    }

    // Overload for backward compatibility
    @Transactional
    public Complaint createComplaint(String title, String description, String category, String location, String citizenName) {
        return createComplaint(title, description, category, location, citizenName, "citizen@civicpulse.com", "");
    }

    private Officer findOfficerForDepartment(String department) {
        if (department == null) return null;
        List<Officer> officers = officerRepository.findAll();
        for (Officer o : officers) {
            if (o.isActive() && o.getDepartment() != null) {
                if (department.equalsIgnoreCase(o.getDepartment()) ||
                    o.getDepartment().toLowerCase().contains(department.toLowerCase()) ||
                    department.toLowerCase().contains(o.getDepartment().toLowerCase())) {
                    return o;
                }
            }
        }
        // Fallback: if roads department, look for roads or public works in officer designation/department
        if (department.toLowerCase().contains("road") || department.toLowerCase().contains("works")) {
            for (Officer o : officers) {
                if (o.isActive() && o.getDepartment() != null &&
                        (o.getDepartment().toLowerCase().contains("works") || o.getDepartment().toLowerCase().contains("road"))) {
                    return o;
                }
            }
        }
        return officers.isEmpty() ? null : officers.get(0);
    }

    /* =========================================================
       STATUS UPDATE LOGIC
    ========================================================= */

    @Transactional
    public Complaint updateStatus(String id, String newStatus, String remarks, String resolution, String updatedBy) {
        Complaint complaint = getComplaint(id);
        if (complaint == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(DATE_TIME_FORMATTER);
        complaint.setUpdatedAt(now);

        String normalizedStatus = newStatus != null ? newStatus.trim() : complaint.getStatus();
        if ("IN_PROGRESS".equalsIgnoreCase(normalizedStatus) || "In Progress".equalsIgnoreCase(normalizedStatus)) {
            complaint.setStatus("In Progress");
            complaint.setInProgressAt(now);
            if (remarks != null && !remarks.trim().isEmpty()) {
                complaint.setRemarks(remarks.trim());
            }

            // Update timeline
            updateOrAddTimelineEvent(complaint, "In Progress", formattedDateTime,
                    "Complaint investigation in progress. " + (remarks != null && !remarks.isBlank() ? "Officer remarks: " + remarks : "Work is ongoing."), "active");

            notificationService.createNotification(
                    complaint.getCitizenEmail(),
                    "Complaint In Progress",
                    "Your complaint " + complaint.getTrackingId() + " is now In Progress.",
                    complaint.getTrackingId(),
                    "STATUS_CHANGE"
            );

        } else if ("RESOLVED".equalsIgnoreCase(normalizedStatus) || "Resolved".equalsIgnoreCase(normalizedStatus)) {
            complaint.setStatus("Resolved");
            complaint.setResolvedAt(now);
            if (resolution != null && !resolution.trim().isEmpty()) {
                complaint.setResolution(resolution.trim());
            } else if (remarks != null && !remarks.trim().isEmpty()) {
                complaint.setResolution(remarks.trim());
            }

            // Mark previous active events completed
            for (TimelineEvent ev : complaint.getTimeline()) {
                if ("active".equalsIgnoreCase(ev.getStatus())) {
                    ev.setStatus("completed");
                }
            }

            // Update timeline
            updateOrAddTimelineEvent(complaint, "Resolved", formattedDateTime,
                    "Complaint has been successfully resolved. " + (complaint.getResolution() != null ? "Resolution: " + complaint.getResolution() : ""), "completed");

            notificationService.createNotification(
                    complaint.getCitizenEmail(),
                    "Complaint Resolved",
                    "Your complaint " + complaint.getTrackingId() + " has been marked as Resolved.",
                    complaint.getTrackingId(),
                    "RESOLUTION"
            );

        } else if ("REOPENED".equalsIgnoreCase(normalizedStatus) || "Reopened".equalsIgnoreCase(normalizedStatus)) {
            complaint.setStatus("Reopened");
            complaint.getTimeline().add(new TimelineEvent(
                    "Complaint Reopened",
                    formattedDateTime,
                    "Complaint reopened by citizen. Reason: " + (remarks != null ? remarks : "Follow-up required."),
                    "active"
            ));

            notificationService.createNotification(
                    complaint.getCitizenEmail(),
                    "Complaint Reopened",
                    "Your complaint " + complaint.getTrackingId() + " has been reopened.",
                    complaint.getTrackingId(),
                    "STATUS_CHANGE"
            );

        } else {
            complaint.setStatus(normalizedStatus);
            complaint.getTimeline().add(new TimelineEvent(
                    "Status Updated: " + normalizedStatus,
                    formattedDateTime,
                    remarks != null && !remarks.isBlank() ? remarks : "Status updated by " + (updatedBy != null ? updatedBy : "Officer"),
                    "active"
            ));
        }

        return complaintRepository.save(complaint);
    }

    private void updateOrAddTimelineEvent(Complaint complaint, String title, String date, String description, String status) {
        boolean updated = false;
        for (TimelineEvent event : complaint.getTimeline()) {
            if (event.getTitle() != null && event.getTitle().equalsIgnoreCase(title)) {
                event.setDate(date);
                event.setDescription(description);
                event.setStatus(status);
                updated = true;
                break;
            }
        }
        if (!updated) {
            complaint.getTimeline().add(new TimelineEvent(title, date, description, status));
        }
    }

    /* =========================================================
       CITIZEN-SPECIFIC QUERIES
    ========================================================= */

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsForCitizen(String email) {
        if (email == null || email.trim().isEmpty()) {
            return List.of();
        }
        return complaintRepository.findByCitizenEmailOrderByCreatedAtDesc(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public long getTotalComplaintsForCitizen(String email) {
        if (email == null || email.trim().isEmpty()) return 0;
        return complaintRepository.countByCitizenEmail(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public long getPendingComplaintsForCitizen(String email) {
        if (email == null || email.trim().isEmpty()) return 0;
        return getComplaintsForCitizen(email).stream()
                .filter(c -> c.getStatus() != null &&
                        (c.getStatus().equalsIgnoreCase("Pending") ||
                         c.getStatus().equalsIgnoreCase("Assigned to Officer") ||
                         c.getStatus().equalsIgnoreCase("Under Review")))
                .count();
    }

    @Transactional(readOnly = true)
    public long getInProgressComplaintsForCitizen(String email) {
        if (email == null || email.trim().isEmpty()) return 0;
        return getComplaintsForCitizen(email).stream()
                .filter(c -> c.getStatus() != null &&
                        (c.getStatus().equalsIgnoreCase("In Progress") ||
                         c.getStatus().equalsIgnoreCase("IN_PROGRESS")))
                .count();
    }

    @Transactional(readOnly = true)
    public long getResolvedComplaintsForCitizen(String email) {
        if (email == null || email.trim().isEmpty()) return 0;
        return getComplaintsForCitizen(email).stream()
                .filter(c -> c.getStatus() != null &&
                        (c.getStatus().equalsIgnoreCase("Resolved") ||
                         c.getStatus().equalsIgnoreCase("RESOLVED")))
                .count();
    }

    @Transactional(readOnly = true)
    public List<Complaint> getRecentComplaintsForCitizen(String email, int limit) {
        return getComplaintsForCitizen(email).stream()
                .limit(limit)
                .collect(Collectors.toList());
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
        return countByStatus("Pending") + countByStatus("Assigned to Officer");
    }

    @Transactional(readOnly = true)
    public long getUnderReviewComplaints() {
        return complaintRepository.findAll()
                .stream()
                .filter(c -> c.getStatus() != null &&
                        (c.getStatus().equalsIgnoreCase("Under Review") ||
                         c.getStatus().equalsIgnoreCase("In Progress") ||
                         c.getStatus().equalsIgnoreCase("IN_PROGRESS") ||
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
    public List<ComplaintDetailsDTO> getComplaintDetailsForOfficer(Officer officer) {
        if (officer == null) {
            return getAllComplaintDetails();
        }
        return getComplaintsForOfficer(officer)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsForOfficer(Officer officer) {
        if (officer == null) {
            return complaintRepository.findAll();
        }

        // Return complaints specifically assigned to this officer OR belonging to this officer's department
        String dept = officer.getDepartment();
        return complaintRepository.findAll().stream()
                .filter(c -> (c.getAssignedOfficer() != null && c.getAssignedOfficer().getId().equals(officer.getId())) ||
                             (dept != null && c.getDepartment() != null &&
                              (c.getDepartment().equalsIgnoreCase(dept) ||
                               c.getDepartment().toLowerCase().contains(dept.toLowerCase()) ||
                               dept.toLowerCase().contains(c.getDepartment().toLowerCase()))))
                .sorted((c1, c2) -> Long.compare(c2.getId() != null ? c2.getId() : 0, c1.getId() != null ? c1.getId() : 0))
                .collect(Collectors.toList());
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
        return getComplaintsForOfficer(officer).stream()
                .filter(c -> c.getStatus() != null && c.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByPriority(Officer officer, String priority) {
        return getComplaintsForOfficer(officer).stream()
                .filter(c -> c.getPriority() != null && c.getPriority().equalsIgnoreCase(priority))
                .collect(Collectors.toList());
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
