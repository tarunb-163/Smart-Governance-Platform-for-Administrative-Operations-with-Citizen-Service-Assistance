package com.civicpulse.service;

import com.civicpulse.model.Complaint;
import com.civicpulse.model.Officer;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.repository.OfficerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OfficerService {

    private final OfficerRepository officerRepository;
    private final ComplaintRepository complaintRepository;

    public OfficerService(OfficerRepository officerRepository, ComplaintRepository complaintRepository) {
        this.officerRepository = officerRepository;
        this.complaintRepository = complaintRepository;
    }

    public Optional<Officer> findByUsername(String username) {
        return officerRepository.findByUsername(username);
    }

    public Optional<Officer> findById(Long id) {
        return officerRepository.findById(id);
    }

    public List<Officer> getAllOfficers() {
        return officerRepository.findAll();
    }

    public Officer save(Officer officer) {
        return officerRepository.save(officer);
    }

    public Officer update(Officer officer) {
        return officerRepository.save(officer);
    }

    public List<Complaint> getAssignedComplaints(String username) {
        Officer officer = officerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Officer not found: " + username));
        return complaintRepository.findByAssignedOfficer(officer);
    }

    @Transactional
    public boolean updateComplaintStatus(Long complaintId, String newStatus, String remarks, String username) {
        Optional<Complaint> optionalComplaint = complaintRepository.findById(complaintId);

        if (optionalComplaint.isPresent()) {
            Complaint complaint = optionalComplaint.get();

            complaint.setStatus(newStatus);
            if (remarks != null && !remarks.trim().isEmpty()) {
                complaint.setRemarks(remarks);
            }
            complaint.setUpdatedAt(LocalDateTime.now());

            if ("IN_PROGRESS".equalsIgnoreCase(newStatus) || "In Progress".equalsIgnoreCase(newStatus)) {
                complaint.setInProgressAt(LocalDateTime.now());
            } else if ("RESOLVED".equalsIgnoreCase(newStatus) || "Resolved".equalsIgnoreCase(newStatus)) {
                complaint.setResolvedAt(LocalDateTime.now());
            }

            complaintRepository.save(complaint);
            return true;
        }
        return false;
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword, String confirmPassword, PasswordEncoder passwordEncoder) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid officer session.");
        }
        Officer officer = officerRepository.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("Officer account not found."));

        if (currentPassword == null || currentPassword.isEmpty() || !passwordEncoder.matches(currentPassword, officer.getPassword())) {
            throw new IllegalArgumentException("Current password does not match our records.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters in length.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirm password do not match.");
        }
        if (passwordEncoder.matches(newPassword, officer.getPassword())) {
            throw new IllegalArgumentException("New password cannot be identical to the current password.");
        }

        officer.setPassword(passwordEncoder.encode(newPassword));
        officerRepository.save(officer);
    }
}
