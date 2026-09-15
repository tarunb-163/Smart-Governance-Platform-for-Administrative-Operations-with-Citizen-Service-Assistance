package com.civicpulse.controller;

import com.civicpulse.model.Citizen;
import com.civicpulse.model.Complaint;
import com.civicpulse.model.Officer;
import com.civicpulse.repository.OfficerRepository;
import com.civicpulse.service.CitizenService;
import com.civicpulse.service.ComplaintService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;

@Controller
public class AdminController {

    private final ComplaintService complaintService;
    private final CitizenService citizenService;
    private final OfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AdminController(
            ComplaintService complaintService,
            CitizenService citizenService,
            OfficerRepository officerRepository,
            PasswordEncoder passwordEncoder) {

        this.complaintService = complaintService;
        this.citizenService = citizenService;
        this.officerRepository = officerRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // =========================================================
    // ADMIN DASHBOARD
    // =========================================================

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {

        long total =
                complaintService.getTotalComplaints();

        long pending =
                complaintService.getPendingComplaints();

        long underReview =
                complaintService.getUnderReviewComplaints();

        long resolved =
                complaintService.getResolvedComplaints();


        model.addAttribute(
                "totalComplaints",
                total
        );

        model.addAttribute(
                "pendingComplaints",
                pending
        );

        model.addAttribute(
                "underReviewComplaints",
                underReview
        );

        model.addAttribute(
                "resolvedComplaints",
                resolved
        );


        double pendingPercentage =
                total == 0
                        ? 0
                        : (pending * 100.0 / total);

        double underReviewPercentage =
                total == 0
                        ? 0
                        : (underReview * 100.0 / total);

        double resolvedPercentage =
                total == 0
                        ? 0
                        : (resolved * 100.0 / total);


        model.addAttribute(
                "pendingPercentage",
                pendingPercentage
        );

        model.addAttribute(
                "underReviewPercentage",
                underReviewPercentage
        );

        model.addAttribute(
                "resolvedPercentage",
                resolvedPercentage
        );


        model.addAttribute(
                "recentComplaints",
                complaintService.getRecentComplaints()
        );


        return "admin/dashboard";
    }


    // =========================================================
    // COMPLAINTS LIST
    // =========================================================

    @GetMapping("/admin/complaints")
    public String complaints(Model model) {

        long total =
                complaintService.getTotalComplaints();

        long pending =
                complaintService.getPendingComplaints();

        long underReview =
                complaintService.getUnderReviewComplaints();

        long resolved =
                complaintService.getResolvedComplaints();


        model.addAttribute(
                "complaints",
                complaintService.getAllComplaints()
        );

        model.addAttribute(
                "totalComplaints",
                total
        );

        model.addAttribute(
                "pendingComplaints",
                pending
        );

        model.addAttribute(
                "underReviewComplaints",
                underReview
        );

        model.addAttribute(
                "resolvedComplaints",
                resolved
        );


        return "admin/complaints";
    }


    // =========================================================
    // COMPLAINT DETAILS
    // =========================================================

    @GetMapping("/admin/complaints/{id}")
    public String complaintDetails(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Complaint complaint =
                complaintService.getComplaintById(id);


        if (complaint == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Complaint #" + id + " not found."
            );

            return "redirect:/admin/complaints";
        }


        model.addAttribute(
                "complaint",
                complaint
        );


        // =====================================================
        // AI PRIORITY ASSESSMENT
        // =====================================================

        String aiPriority =
                calculateAiPriority(complaint);

        int aiConfidence =
                calculateAiConfidence(
                        complaint,
                        aiPriority
                );

        String aiReason =
                generateAiReason(
                        complaint,
                        aiPriority
                );


        model.addAttribute(
                "aiPriority",
                aiPriority
        );

        model.addAttribute(
                "aiConfidence",
                aiConfidence
        );

        model.addAttribute(
                "aiPriorityReason",
                aiReason
        );


        // =====================================================
        // DETAILED STATUS
        // =====================================================

        String currentStatus =
                complaint.getStatus();

        String statusDetail =
                getStatusDetail(currentStatus);

        String statusAction =
                getStatusAction(currentStatus);


        model.addAttribute(
                "statusDetail",
                statusDetail
        );

        model.addAttribute(
                "statusAction",
                statusAction
        );


        return "admin/complaint-details";
    }


    // =========================================================
    // AI PRIORITY CALCULATION
    // =========================================================

    private String calculateAiPriority(
            Complaint complaint) {

        int score = 0;


        String title =
                safeLower(complaint.getTitle());

        String description =
                safeLower(complaint.getDescription());

        String category =
                safeLower(complaint.getCategory());

        String location =
                safeLower(complaint.getLocation());


        String text =
                title + " "
                        + description + " "
                        + category + " "
                        + location;


        // -----------------------------------------------------
        // CRITICAL SAFETY INDICATORS
        // -----------------------------------------------------

        String[] criticalKeywords = {

                "fire",
                "explosion",
                "electric pole fallen",
                "electric pole collapse",
                "live wire",
                "exposed wire",
                "gas leak",
                "building collapse",
                "collapsed building",
                "accident",
                "life threatening",
                "danger to life",
                "electrocution",
                "flood",
                "major flooding"

        };


        for (String keyword : criticalKeywords) {

            if (text.contains(keyword)) {

                score += 40;
            }
        }


        // -----------------------------------------------------
        // HIGH RISK INDICATORS
        // -----------------------------------------------------

        String[] highKeywords = {

                "dangerous",
                "unsafe",
                "broken pole",
                "fallen tree",
                "open manhole",
                "sewage overflow",
                "water leakage",
                "road damage",
                "large pothole",
                "street light",
                "accident risk",
                "school",
                "hospital",
                "children",
                "elderly"

        };


        for (String keyword : highKeywords) {

            if (text.contains(keyword)) {

                score += 15;
            }
        }


        // -----------------------------------------------------
        // CATEGORY BASED RISK
        // -----------------------------------------------------

        if (category.contains("electric")) {

            score += 25;
        }

        if (category.contains("fire")) {

            score += 35;
        }

        if (category.contains("water")) {

            score += 10;
        }

        if (category.contains("sanitation")) {

            score += 8;
        }

        if (category.contains("road")) {

            score += 12;
        }


        // -----------------------------------------------------
        // LOCATION BASED RISK
        // -----------------------------------------------------

        if (location.contains("school")) {

            score += 15;
        }

        if (location.contains("hospital")) {

            score += 15;
        }

        if (location.contains("market")) {

            score += 8;
        }

        if (location.contains("main road")) {

            score += 8;
        }


        // -----------------------------------------------------
        // FINAL PRIORITY
        // -----------------------------------------------------

        if (score >= 60) {

            return "CRITICAL";

        } else if (score >= 35) {

            return "HIGH";

        } else if (score >= 15) {

            return "MEDIUM";

        } else {

            return "LOW";
        }
    }


    // =========================================================
    // AI CONFIDENCE
    // =========================================================

    private int calculateAiConfidence(
            Complaint complaint,
            String aiPriority) {

        String title =
                safeLower(complaint.getTitle());

        String description =
                safeLower(complaint.getDescription());

        String category =
                safeLower(complaint.getCategory());

        String location =
                safeLower(complaint.getLocation());


        int confidence = 70;


        if (!title.isBlank()) {

            confidence += 5;
        }

        if (!description.isBlank()) {

            confidence += 8;
        }

        if (!category.isBlank()) {

            confidence += 5;
        }

        if (!location.isBlank()) {

            confidence += 5;
        }


        if ("CRITICAL".equals(aiPriority)) {

            confidence += 4;
        }


        return Math.min(
                confidence,
                97
        );
    }


    // =========================================================
    // AI REASON
    // =========================================================

    private String generateAiReason(
            Complaint complaint,
            String aiPriority) {

        String text =
                safeLower(
                        safe(complaint.getTitle())
                                + " "
                                + safe(complaint.getDescription())
                                + " "
                                + safe(complaint.getCategory())
                                + " "
                                + safe(complaint.getLocation())
                );


        if ("CRITICAL".equals(aiPriority)) {

            return "High-risk safety indicators were detected in the complaint details. Immediate administrative attention is recommended.";
        }


        if ("HIGH".equals(aiPriority)) {

            return "The complaint contains significant risk or service-impact indicators and should be prioritised for timely action.";
        }


        if ("MEDIUM".equals(aiPriority)) {

            return "The complaint indicates a moderate service issue. Timely departmental review is recommended.";
        }


        return "No major emergency or public-safety indicators were detected. The complaint can follow the normal processing workflow.";
    }


    // =========================================================
    // DETAILED STATUS DESCRIPTION
    // =========================================================

    private String getStatusDetail(
            String status) {

        if (status == null ||
                status.trim().isEmpty()) {

            return "The complaint status has not been specified.";
        }


        String normalized =
                status.trim()
                        .toLowerCase(Locale.ROOT);


        switch (normalized) {

            case "pending":

                return "The complaint has been received and is waiting for initial administrative processing.";

            case "under review":

                return "The complaint is currently being reviewed to determine the required action and responsible department.";

            case "in progress":

                return "Action is currently being taken on the complaint by the responsible department or assigned officer.";

            case "resolved":

                return "The required action has been completed and the complaint has been marked as resolved.";

            case "rejected":

                return "The complaint has been rejected and requires an appropriate reason or clarification.";

            default:

                return "The complaint is currently in the '"
                        + status
                        + "' processing stage.";
        }
    }


    // =========================================================
    // NEXT ACTION
    // =========================================================

    private String getStatusAction(
            String status) {

        if (status == null ||
                status.trim().isEmpty()) {

            return "Review the complaint and assign an appropriate processing status.";
        }


        String normalized =
                status.trim()
                        .toLowerCase(Locale.ROOT);


        switch (normalized) {

            case "pending":

                return "Review the complaint and assign it to the appropriate officer or department.";

            case "under review":

                return "Complete the assessment and move the complaint to In Progress when action begins.";

            case "in progress":

                return "Continue the assigned work and provide a clear progress update for the citizen.";

            case "resolved":

                return "Verify the resolution and ensure the citizen receives the final update.";

            case "rejected":

                return "Ensure a valid rejection reason is recorded and communicated.";

            default:

                return "Review the current complaint status and take the appropriate administrative action.";
        }
    }


    // =========================================================
    // UPDATE COMPLAINT STATUS
    // =========================================================

    @PostMapping("/admin/complaints/{id}/status")
    public String updateComplaintStatus(
            @PathVariable Long id,
            @RequestParam(
                    value = "status",
                    required = false
            )
            String status,
            RedirectAttributes redirectAttributes) {

        Complaint complaint =
                complaintService.getComplaintById(id);


        if (complaint == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Complaint #" + id + " not found."
            );

            return "redirect:/admin/complaints";
        }


        if (status == null ||
                status.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please select a complaint status."
            );

            return "redirect:/admin/complaints/" + id;
        }


        complaint.setStatus(
                status.trim()
        );


        complaintService.saveComplaint(
                complaint
        );


        redirectAttributes.addFlashAttribute(
                "success",
                "Complaint status updated successfully."
        );


        return "redirect:/admin/complaints/" + id;
    }


    // =========================================================
    // OFFICER MANAGEMENT
    // =========================================================

    @GetMapping("/admin/officers")
    public String officers(Model model) {

        List<Officer> officers =
                officerRepository.findAll();


        long totalOfficers =
                officers.size();


        long activeOfficers =
                officers.stream()
                        .filter(Officer::isActive)
                        .count();


        long inactiveOfficers =
                officers.stream()
                        .filter(officer -> !officer.isActive())
                        .count();


        model.addAttribute(
                "officers",
                officers
        );

        model.addAttribute(
                "totalOfficers",
                totalOfficers
        );

        model.addAttribute(
                "activeOfficers",
                activeOfficers
        );

        model.addAttribute(
                "inactiveOfficers",
                inactiveOfficers
        );


        return "admin/officers";
    }


    // =========================================================
    // ADD / UPDATE OFFICER
    // =========================================================

    @PostMapping("/admin/officers/save")
    public String saveOfficer(

            @RequestParam(
                    value = "id",
                    required = false
            )
            Long id,

            @RequestParam("username")
            String username,

            @RequestParam("fullName")
            String fullName,

            @RequestParam(
                    value = "employeeId",
                    required = false
            )
            String employeeId,

            @RequestParam(
                    value = "email",
                    required = false
            )
            String email,

            @RequestParam(
                    value = "phone",
                    required = false
            )
            String phone,

            @RequestParam(
                    value = "department",
                    required = false
            )
            String department,

            @RequestParam(
                    value = "designation",
                    required = false
            )
            String designation,

            @RequestParam(
                    value = "jurisdiction",
                    required = false
            )
            String jurisdiction,

            @RequestParam(
                    value = "officeLocation",
                    required = false
            )
            String officeLocation,

            @RequestParam(
                    value = "role",
                    required = false
            )
            String role,

            @RequestParam(
                    value = "password",
                    required = false
            )
            String password,

            @RequestParam(
                    value = "active",
                    defaultValue = "false"
            )
            boolean active,

            RedirectAttributes redirectAttributes) {


        // -----------------------------------------------------
        // VALIDATION
        // -----------------------------------------------------

        if (username == null ||
                username.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Username is required."
            );

            return "redirect:/admin/officers";
        }


        if (fullName == null ||
                fullName.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Full name is required."
            );

            return "redirect:/admin/officers";
        }


        // -----------------------------------------------------
        // CREATE / UPDATE
        // -----------------------------------------------------

        Officer officer;


        if (id == null) {

            officer = new Officer();


            // Username duplicate check

            if (officerRepository
                    .findByUsername(username.trim())
                    .isPresent()) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Username already exists."
                );

                return "redirect:/admin/officers";
            }

        } else {

            officer =
                    officerRepository
                            .findById(id)
                            .orElse(null);


            if (officer == null) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Officer not found."
                );

                return "redirect:/admin/officers";
            }
        }


        // -----------------------------------------------------
        // UPDATE BASIC INFORMATION
        // -----------------------------------------------------

        officer.setUsername(
                username.trim()
        );

        officer.setFullName(
                fullName.trim()
        );

        officer.setEmployeeId(
                blankToNull(employeeId)
        );

        officer.setEmail(
                blankToNull(email)
        );

        officer.setPhone(
                blankToNull(phone)
        );

        officer.setDepartment(
                blankToNull(department)
        );

        officer.setDesignation(
                blankToNull(designation)
        );

        officer.setJurisdiction(
                blankToNull(jurisdiction)
        );

        officer.setOfficeLocation(
                blankToNull(officeLocation)
        );


        officer.setRole(
                role == null ||
                        role.trim().isEmpty()
                        ? "OFFICER"
                        : role.trim().toUpperCase()
        );


        officer.setActive(
                active
        );


        // -----------------------------------------------------
        // PASSWORD
        // -----------------------------------------------------

        if (id == null) {

            if (password == null ||
                    password.trim().isEmpty()) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Password is required for a new officer."
                );

                return "redirect:/admin/officers";
            }


            officer.setPassword(
                    passwordEncoder.encode(
                            password.trim()
                    )
            );

        } else {

            /*
             * Editing officer:
             * Password is changed only if
             * admin enters a new password.
             */

            if (password != null &&
                    !password.trim().isEmpty()) {

                officer.setPassword(
                        passwordEncoder.encode(
                                password.trim()
                        )
                );
            }
        }


        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        officerRepository.save(
                officer
        );


        redirectAttributes.addFlashAttribute(
                "success",
                id == null
                        ? "Officer added successfully."
                        : "Officer updated successfully."
        );


        return "redirect:/admin/officers";
    }


    // =========================================================
    // EDIT OFFICER
    // =========================================================

    @GetMapping("/admin/officers/edit/{id}")
    public String editOfficer(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Officer officer =
                officerRepository
                        .findById(id)
                        .orElse(null);


        if (officer == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Officer not found."
            );

            return "redirect:/admin/officers";
        }


        List<Officer> officers =
                officerRepository.findAll();


        model.addAttribute(
                "officers",
                officers
        );


        model.addAttribute(
                "editOfficer",
                officer
        );


        model.addAttribute(
                "totalOfficers",
                officers.size()
        );


        model.addAttribute(
                "activeOfficers",
                officers.stream()
                        .filter(Officer::isActive)
                        .count()
        );


        model.addAttribute(
                "inactiveOfficers",
                officers.stream()
                        .filter(officerItem ->
                                !officerItem.isActive())
                        .count()
        );


        return "admin/officers";
    }


    // =========================================================
    // DELETE OFFICER
    // =========================================================

    @PostMapping("/admin/officers/{id}/delete")
    public String deleteOfficer(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {

        Officer officer = officerRepository.findById(id).orElse(null);

        if (officer == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Officer not found."
            );

            return "redirect:/admin/officers";
        }

        /*
         * First remove this officer from all complaints.
         * Otherwise MySQL/PostgreSQL foreign-key constraint
         * will prevent officer deletion.
         */
        List<Complaint> complaints =
                complaintService.getAllComplaints();

        for (Complaint complaint : complaints) {

            if (complaint.getAssignedOfficer() != null
                    && id.equals(
                    complaint.getAssignedOfficer().getId())) {

                complaint.setAssignedOfficer(null);

                complaint.setAssignedAt(null);

                complaintService.saveComplaint(complaint);
            }
        }

        /*
         * Now it is safe to delete the officer.
         */
        officerRepository.delete(officer);

        redirectAttributes.addFlashAttribute(
                "success",
                "Officer deleted successfully."
        );

        return "redirect:/admin/officers";
    }


    // =========================================================
    // CITIZENS
    // =========================================================

    @GetMapping("/admin/citizens")
    public String citizens(Model model) {

        List<Citizen> citizens =
                citizenService.getAllCitizens();


        model.addAttribute(
                "citizens",
                citizens
        );

        model.addAttribute(
                "totalCitizens",
                citizenService.getTotalCitizensCount()
        );

        model.addAttribute(
                "activeCitizens",
                citizenService.getActiveCitizensCount()
        );

        model.addAttribute(
                "inactiveCitizens",
                citizenService.getInactiveCitizensCount()
        );


        return "admin/citizens";
    }


    // =========================================================
    // TOGGLE CITIZEN STATUS
    // =========================================================

    @PostMapping("/admin/citizens/{id}/toggle-status")
    public String toggleCitizenStatus(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            citizenService.toggleCitizenStatus(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Citizen status updated successfully."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Unable to update citizen status."
            );
        }


        return "redirect:/admin/citizens";
    }


    // =========================================================
    // REPORTS
    // =========================================================

    @GetMapping("/admin/reports")
    public String reports(Model model) {

        model.addAttribute(
                "totalComplaints",
                complaintService.getTotalComplaints()
        );

        model.addAttribute(
                "pendingComplaints",
                complaintService.getPendingComplaints()
        );

        model.addAttribute(
                "underReviewComplaints",
                complaintService.getUnderReviewComplaints()
        );

        model.addAttribute(
                "resolvedComplaints",
                complaintService.getResolvedComplaints()
        );

        model.addAttribute(
                "complaints",
                complaintService.getAllComplaints()
        );


        return "admin/reports";
    }


    // =========================================================
    // ADMIN PROFILE - GET
    // =========================================================

    @GetMapping("/admin/profile")
    public String profile(Model model) {

        model.addAttribute(
                "adminName",
                "Yogendra"
        );

        model.addAttribute(
                "adminRole",
                "System Admin"
        );

        model.addAttribute(
                "adminEmail",
                "admin@civicpulse.com"
        );

        model.addAttribute(
                "accountType",
                "Administrator"
        );


        return "admin/profile";
    }


    // =========================================================
    // ADMIN PROFILE - POST
    // =========================================================

    @PostMapping("/admin/profile")
    public String updateAdminProfile(

            @RequestParam(
                    value = "displayName",
                    required = false
            )
            String displayName,

            @RequestParam(
                    value = "email",
                    required = false
            )
            String email,

            RedirectAttributes redirectAttributes) {


        if (displayName == null ||
                displayName.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Display name cannot be empty."
            );

            return "redirect:/admin/profile";
        }


        if (email == null ||
                email.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Email cannot be empty."
            );

            return "redirect:/admin/profile";
        }


        redirectAttributes.addFlashAttribute(
                "success",
                "Profile updated successfully!"
        );


        return "redirect:/admin/profile";
    }


    // =========================================================
    // SETTINGS
    // =========================================================

    @GetMapping("/admin/settings")
    public String settings() {

        return "admin/settings";
    }


    // =========================================================
    // SAFE STRING HELPERS
    // =========================================================

    private String safe(String value) {

        return value == null
                ? ""
                : value;
    }


    private String safeLower(String value) {

        return safe(value)
                .toLowerCase(Locale.ROOT);
    }


    private String blankToNull(String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return null;
        }

        return value.trim();
    }

}