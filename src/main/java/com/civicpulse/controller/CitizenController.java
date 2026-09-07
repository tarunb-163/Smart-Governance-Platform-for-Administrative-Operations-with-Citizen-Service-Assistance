package com.civicpulse.controller;

import com.civicpulse.entites.Citizen;
import com.civicpulse.entites.Complaint;
import com.civicpulse.repositories.CitizenRepository;
import com.civicpulse.repositories.ComplaintRepository;
import com.civicpulse.services.ComplaintService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CitizenController {

    private final CitizenRepository citizenRepository;
    private final ComplaintService complaintService;
    private final ComplaintRepository complaintRepository;

    public CitizenController(
            CitizenRepository citizenRepository,
            ComplaintService complaintService,
            ComplaintRepository complaintRepository) {

        this.citizenRepository = citizenRepository;
        this.complaintService = complaintService;
        this.complaintRepository = complaintRepository;
    }

    // =========================================================
    // CITIZEN DASHBOARD
    // =========================================================

    @GetMapping("/citizen/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model) {

        Citizen citizen = getLoggedInCitizen(authentication);

        var complaints = complaintService
                .getComplaintsForCitizen(citizen.getEmail());

        long totalComplaints = complaints.size();

        long pendingComplaints = complaints.stream()
                .filter(c -> "PENDING".equalsIgnoreCase(c.getStatus()))
                .count();

        long inProgressComplaints = complaints.stream()
                .filter(c -> "IN_PROGRESS".equalsIgnoreCase(c.getStatus()))
                .count();

        long resolvedComplaints = complaints.stream()
                .filter(c -> "RESOLVED".equalsIgnoreCase(c.getStatus()))
                .count();

        model.addAttribute("citizen", citizen);
        model.addAttribute("complaints", complaints);

        model.addAttribute("totalComplaints", totalComplaints);
        model.addAttribute("pendingComplaints", pendingComplaints);
        model.addAttribute("inProgressComplaints", inProgressComplaints);
        model.addAttribute("resolvedComplaints", resolvedComplaints);

        return "citizen/dashboard";
    }


    // =========================================================
    // MY COMPLAINTS
    // =========================================================

    @GetMapping("/citizen/complaints")
    public String complaints(
            Authentication authentication,
            Model model) {

        Citizen citizen = getLoggedInCitizen(authentication);

        var complaints = complaintService
                .getComplaintsForCitizen(citizen.getEmail());

        model.addAttribute("citizen", citizen);
        model.addAttribute("complaints", complaints);

        return "citizen/complaints";
    }


    // =========================================================
    // TRACK COMPLAINT
    // =========================================================

    @GetMapping("/citizen/track")
    public String trackComplaint(
            @RequestParam(required = false) String complaintNumber,
            Authentication authentication,
            Model model) {

        Citizen citizen = getLoggedInCitizen(authentication);

        model.addAttribute("citizen", citizen);

        /*
         * First time page open:
         * only tracking/search form will be displayed.
         */
        if (complaintNumber == null
                || complaintNumber.trim().isEmpty()) {

            return "citizen/track";
        }

        String number = complaintNumber.trim();

        /*
         * Find complaint using complaint number.
         */
        Complaint complaint = complaintRepository
                .findByComplaintNumber(number)
                .orElse(null);

        /*
         * Complaint not found.
         */
        if (complaint == null) {

            model.addAttribute(
                    "errorMessage",
                    "Complaint not found. Please check your complaint number."
            );

            model.addAttribute(
                    "searchedComplaintNumber",
                    number
            );

            return "citizen/track";
        }

        /*
         * Security check:
         *
         * Complaint entity does NOT have a Citizen relationship.
         * Therefore ownership is checked using citizenEmail.
         */
        String complaintEmail = complaint.getCitizenEmail();
        String citizenEmail = citizen.getEmail();

        if (complaintEmail == null
                || citizenEmail == null
                || !complaintEmail.equalsIgnoreCase(citizenEmail)) {

            model.addAttribute(
                    "errorMessage",
                    "You are not authorized to view this complaint."
            );

            model.addAttribute(
                    "searchedComplaintNumber",
                    number
            );

            return "citizen/track";
        }

        /*
         * Everything is valid.
         * Send complaint object to Thymeleaf.
         */
        model.addAttribute("complaint", complaint);

        return "citizen/track";
    }


    // =========================================================
    // CITIZEN NOTIFICATIONS
    // =========================================================

    @GetMapping("/citizen/notifications")
    public String notifications(
            Authentication authentication,
            Model model) {

        Citizen citizen = getLoggedInCitizen(authentication);

        model.addAttribute("citizen", citizen);

        return "citizen/notifications";
    }


    // =========================================================
    // CITIZEN PROFILE - GET
    // =========================================================

    @GetMapping("/citizen/profile")
    public String profile(
            Authentication authentication,
            Model model) {

        Citizen citizen = getLoggedInCitizen(authentication);

        model.addAttribute("citizen", citizen);

        return "citizen/profile";
    }


    // =========================================================
    // CITIZEN PROFILE - UPDATE
    // =========================================================

    @PostMapping("/citizen/profile")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Citizen citizen = getLoggedInCitizen(authentication);

        citizen.setFullName(fullName);

        if (email != null && !email.trim().isEmpty()) {
            citizen.setEmail(email.trim());
        }

        if (phone != null) {
            citizen.setPhone(phone.trim());
        }

        if (address != null) {
            citizen.setAddress(address.trim());
        }

        citizenRepository.save(citizen);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Profile updated successfully."
        );

        return "redirect:/citizen/profile";
    }


    // =========================================================
    // HELPER METHOD
    // =========================================================

    private Citizen getLoggedInCitizen(
            Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null) {

            throw new RuntimeException(
                    "Citizen authentication not found."
            );
        }

        String username = authentication.getName();

        return citizenRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Citizen not found: " + username
                        )
                );
    }
}