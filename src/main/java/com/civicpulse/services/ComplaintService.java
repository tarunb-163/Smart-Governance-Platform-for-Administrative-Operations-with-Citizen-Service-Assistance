package com.civicpulse.services;

import com.civicpulse.dto.AttachmentDTO;
import com.civicpulse.dto.ComplaintDetailsDTO;
import com.civicpulse.entites.Complaint;
import com.civicpulse.entites.ComplaintAttachment;
import com.civicpulse.entites.Officer;
import com.civicpulse.repositories.ComplaintRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    public ComplaintService(
            ComplaintRepository complaintRepository) {

        this.complaintRepository = complaintRepository;
    }


    // =========================================================
    // GET COMPLAINTS FOR CITIZEN
    // =========================================================

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsForCitizen(
            String citizenEmail) {

        if (citizenEmail == null ||
                citizenEmail.isBlank()) {

            return Collections.emptyList();
        }

        return complaintRepository
                .findByCitizenEmailOrderByCreatedAtDesc(
                        citizenEmail
                );
    }


    // =========================================================
    // API / DTO RETRIEVAL
    // =========================================================

    @Transactional(readOnly = true)
    public ComplaintDetailsDTO getComplaintDetailsById(
            String id) {

        if (id == null || id.isBlank()) {
            return null;
        }

        Optional<Complaint> complaintOptional =
                findByStringIdentifier(id);

        if (complaintOptional.isEmpty()) {
            return null;
        }

        Complaint complaint =
                complaintOptional.get();

        // Initialize lazy attachments while transaction is active
        complaint.getAttachments().size();

        return mapToDTO(complaint);
    }


    // =========================================================
    // OFFICER COMPLAINTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsForOfficer(
            Officer officer) {

        if (officer == null) {
            return Collections.emptyList();
        }

        return complaintRepository
                .findByAssignedOfficer(officer);
    }


    // =========================================================
    // BASIC FIND
    // =========================================================

    @Transactional(readOnly = true)
    public Optional<Complaint> findById(
            Long id) {

        return complaintRepository.findById(id);
    }


    @Transactional(readOnly = true)
    public Optional<Complaint> findByComplaintNumber(
            String complaintNumber) {

        if (complaintNumber == null ||
                complaintNumber.isBlank()) {

            return Optional.empty();
        }

        return complaintRepository
                .findByComplaintNumber(
                        complaintNumber
                );
    }


    // =========================================================
    // SAVE
    // =========================================================

    @Transactional
    public Complaint save(
            Complaint complaint) {

        return complaintRepository.save(
                complaint
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Transactional
    public Complaint update(
            Complaint complaint) {

        return complaintRepository.save(
                complaint
        );
    }


    // =========================================================
    // UPDATE COMPLAINT PROGRESS
    // =========================================================

    @Transactional
    public Complaint updateComplaintProgress(
            Long complaintId,
            String progressUpdate) {

        Optional<Complaint> complaintOptional =
                complaintRepository.findById(complaintId);

        if (complaintOptional.isEmpty()) {
            return null;
        }

        Complaint complaint =
                complaintOptional.get();

        complaint.setProgressUpdate(progressUpdate);

        return complaintRepository.save(complaint);
    }


    // =========================================================
    // OFFICER STATUS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByStatus(
            Officer officer,
            String status) {

        if (officer == null ||
                status == null ||
                status.isBlank()) {

            return Collections.emptyList();
        }

        return complaintRepository
                .findByAssignedOfficerAndStatus(
                        officer,
                        status
                );
    }


    // =========================================================
    // OFFICER PRIORITY
    // =========================================================

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByPriority(
            Officer officer,
            String priority) {

        if (officer == null ||
                priority == null ||
                priority.isBlank()) {

            return Collections.emptyList();
        }

        return complaintRepository
                .findByAssignedOfficerAndPriority(
                        officer,
                        priority
                );
    }


    // =========================================================
    // FIND BY ID / COMPLAINT NUMBER
    // =========================================================

    private Optional<Complaint> findByStringIdentifier(
            String identifier) {

        if (identifier == null ||
                identifier.isBlank()) {

            return Optional.empty();
        }

        // First try numeric database ID
        try {

            Long numericId =
                    Long.parseLong(identifier);

            Optional<Complaint> byId =
                    complaintRepository
                            .findByIdWithAttachments(
                                    numericId
                            );

            if (byId.isPresent()) {
                return byId;
            }

        } catch (NumberFormatException ignored) {

            // Not a numeric ID.
            // Continue with complaint number.
        }

        return complaintRepository
                .findByComplaintNumberWithAttachments(
                        identifier
                );
    }


    // =========================================================
    // DTO MAPPING
    // =========================================================

    private ComplaintDetailsDTO mapToDTO(
            Complaint complaint) {

        ComplaintDetailsDTO dto =
                new ComplaintDetailsDTO();


        // Complaint ID
        dto.setId(
                complaint.getComplaintNumber() != null
                        ? complaint.getComplaintNumber()
                        : String.valueOf(
                        complaint.getId()
                )
        );


        // =====================================================
        // BASIC INFORMATION
        // =====================================================

        dto.setTitle(
                complaint.getTitle()
        );

        dto.setDescription(
                complaint.getDescription()
        );

        dto.setLocation(
                complaint.getLocation()
        );

        dto.setDepartment(
                complaint.getDepartment()
        );

        dto.setCategory(
                complaint.getCategory()
        );

        dto.setPriority(
                complaint.getPriority()
        );

        dto.setStatus(
                complaint.getStatus()
        );


        // =====================================================
        // PROGRESS UPDATE - NEW
        // =====================================================

        dto.setProgressUpdate(
                complaint.getProgressUpdate()
        );


        // =====================================================
        // RESOLUTION
        // =====================================================

        dto.setResolution(
                complaint.getResolution()
        );

        dto.setRemarks(
                complaint.getRemarks()
        );


        // =====================================================
        // DATES
        // =====================================================

        dto.setCreatedAt(
                complaint.getCreatedAt()
        );

        dto.setAssignedAt(
                complaint.getAssignedAt()
        );

        dto.setUpdatedAt(
                complaint.getUpdatedAt()
        );

        dto.setResolvedAt(
                complaint.getResolvedAt()
        );


        // =====================================================
        // ATTACHMENTS
        // =====================================================

        if (complaint.getAttachments() != null &&
                !complaint.getAttachments().isEmpty()) {

            List<AttachmentDTO> attachmentDTOs =
                    complaint.getAttachments()
                            .stream()
                            .map(
                                    this::mapAttachmentToDTO
                            )
                            .collect(
                                    Collectors.toList()
                            );

            dto.setAttachments(
                    attachmentDTOs
            );

        } else {

            dto.setAttachments(
                    Collections.emptyList()
            );
        }

        return dto;
    }


    // =========================================================
    // ATTACHMENT DTO
    // =========================================================

    private AttachmentDTO mapAttachmentToDTO(
            ComplaintAttachment attachment) {

        AttachmentDTO dto =
                new AttachmentDTO();

        dto.setId(
                attachment.getId()
        );

        dto.setFileName(
                attachment.getFileName()
        );

        dto.setFileType(
                attachment.getFileType()
        );

        dto.setFileSize(
                attachment.getFileSize()
        );

        dto.setUploadedAt(
                attachment.getUploadedAt()
        );


        // Attachment URL
        if (attachment.getStoredFileName() != null &&
                !attachment.getStoredFileName().isBlank()) {

            dto.setFileUrl(
                    "/uploads/"
                            + attachment.getStoredFileName()
            );

        } else {

            dto.setFileUrl("#");
        }

        return dto;
    }


    // =========================================================
    // ADMIN METHODS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Complaint> getAllComplaints() {

        return complaintRepository.findAll();
    }


    @Transactional(readOnly = true)
    public Complaint getComplaintById(Long id) {

        return complaintRepository
                .findById(id)
                .orElse(null);
    }


    @Transactional(readOnly = true)
    public long getTotalComplaints() {

        return complaintRepository.count();
    }


    @Transactional(readOnly = true)
    public long getPendingComplaints() {

        return complaintRepository.findAll()
                .stream()
                .filter(complaint ->
                        complaint.getStatus() != null &&
                                complaint.getStatus()
                                        .equalsIgnoreCase("PENDING")
                )
                .count();
    }


    @Transactional(readOnly = true)
    public long getUnderReviewComplaints() {

        return complaintRepository.findAll()
                .stream()
                .filter(complaint ->
                        complaint.getStatus() != null &&
                                (
                                        complaint.getStatus()
                                                .equalsIgnoreCase("UNDER REVIEW")
                                                ||
                                                complaint.getStatus()
                                                        .equalsIgnoreCase("UNDER_REVIEW")
                                )
                )
                .count();
    }


    @Transactional(readOnly = true)
    public long getResolvedComplaints() {

        return complaintRepository.findAll()
                .stream()
                .filter(complaint ->
                        complaint.getStatus() != null &&
                                complaint.getStatus()
                                        .equalsIgnoreCase("RESOLVED")
                )
                .count();
    }


    @Transactional(readOnly = true)
    public List<Complaint> getRecentComplaints() {

        return complaintRepository.findAll()
                .stream()
                .sorted(
                        (c1, c2) -> {

                            if (c1.getCreatedAt() == null) {
                                return 1;
                            }

                            if (c2.getCreatedAt() == null) {
                                return -1;
                            }

                            return c2.getCreatedAt()
                                    .compareTo(
                                            c1.getCreatedAt()
                                    );
                        }
                )
                .limit(5)
                .collect(Collectors.toList());
    }


    @Transactional
    public Complaint saveComplaint(Complaint complaint) {

        return complaintRepository.save(complaint);
    }
}