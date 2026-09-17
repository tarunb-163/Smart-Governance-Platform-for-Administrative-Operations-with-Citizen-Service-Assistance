package com.civicpulse.controller;

import com.civicpulse.model.Complaint;
import com.civicpulse.model.Notification;
import com.civicpulse.model.Officer;
import com.civicpulse.service.ComplaintService;
import com.civicpulse.service.NotificationService;
import com.civicpulse.service.OfficerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class OfficerController {

    private final ComplaintService complaintService;
    private final OfficerService officerService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    public OfficerController(ComplaintService complaintService,
                             OfficerService officerService,
                             NotificationService notificationService,
                             PasswordEncoder passwordEncoder) {
        this.complaintService = complaintService;
        this.officerService = officerService;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
    }

    @ModelAttribute
    public void populateOfficerCommonAttributes(Principal principal, Model model) {
        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("loggedInUserName", username);
            model.addAttribute("loggedInUserRole", "Officer");

            Optional<Officer> officerOpt = officerService.findByUsername(username);
            if (officerOpt.isPresent()) {
                Officer officer = officerOpt.get();
                model.addAttribute("officer", officer);
                model.addAttribute("officerName", officer.getFullName());
                model.addAttribute("officerDept", officer.getDepartment());
                model.addAttribute("profileInitials", getInitials(officer.getFullName()));
            }

            List<Notification> officerNotifications = notificationService.getNotificationsForOfficer(username);
            model.addAttribute("notifications", officerNotifications);
            model.addAttribute("unreadNotificationCount", notificationService.getUnreadCountForOfficer(username));
        } else {
            model.addAttribute("notifications", Collections.emptyList());
            model.addAttribute("unreadNotificationCount", 0);
        }
    }

    @GetMapping({ "/", "/portal" })
    public String home(Model model) {
        model.addAttribute("totalComplaints", complaintService.getTotalComplaints());
        model.addAttribute("pendingComplaints", complaintService.getPendingComplaints());
        model.addAttribute("resolvedComplaints", complaintService.getResolvedComplaints());
        return "portal";
    }

    @GetMapping({ "/officer/login", "/officer/login/{dept}" })
    public String officerLogin(Principal principal,
            @RequestParam(value = "dept", required = false) String deptParam,
            @PathVariable(value = "dept", required = false) String deptPath,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (principal != null) {
            model.addAttribute("loggedInUser", principal.getName());
        }

        String activeDept = deptParam != null && !deptParam.trim().isEmpty() ? deptParam.trim()
                : (deptPath != null ? deptPath.trim() : "all");
        model.addAttribute("selectedDept", activeDept.toLowerCase());

        if (error != null) {
            if ("unauthorized".equalsIgnoreCase(error)) {
                model.addAttribute("loginErrorMessage",
                        "Access Denied: Citizen credentials cannot access the Field Officer console.");
            } else {
                model.addAttribute("loginErrorMessage",
                        "Invalid officer credentials. Please verify your department username and password.");
            }
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Officer signed out successfully.");
        }

        return "officer/login";
    }

    @GetMapping("/officer/logout")
    public String officerLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return "redirect:/officer/login?logout";
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/officer/login";
    }

    @GetMapping("/officer/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalComplaints", complaintService.getTotalComplaints());
        model.addAttribute("pendingComplaints", complaintService.getPendingComplaints());
        model.addAttribute("resolvedComplaints", complaintService.getResolvedComplaints());
        return "officer/dashboard";
    }

    @GetMapping("/officer/complaints")
    public String complaints(Model model) {
        model.addAttribute("complaints", complaintService.getAllComplaints());
        return "officer/complaints";
    }

    @GetMapping("/officer/complaint-details")
    public String complaintDetails(@RequestParam(value = "id", required = false) String id, Model model) {
        if (id != null && !id.trim().isEmpty()) {
            Complaint complaint = complaintService.getComplaint(id.trim());
            if (complaint != null) {
                model.addAttribute("complaint", complaint);
            }
        }
        return "officer/complaint-details";
    }

    @GetMapping("/officer/update-complaint")
    public String updateComplaint() {
        return "officer/update-complaint";
    }

    @GetMapping("/officer/profile")
    public String profile(Principal principal, Model model) {
        if (principal != null) {
            Optional<Officer> officerOpt = officerService.findByUsername(principal.getName());
            if (officerOpt.isPresent()) {
                Officer officer = officerOpt.get();
                model.addAttribute("officer", officer);
                model.addAttribute("profileInitials", getInitials(officer.getFullName()));
            }
        }
        return "officer/profile";
    }

    @PostMapping("/officer/profile")
    public String updateProfile(Principal principal, Officer formOfficer, RedirectAttributes redirectAttributes) {
        if (principal != null) {
            Optional<Officer> officerOpt = officerService.findByUsername(principal.getName());
            if (officerOpt.isPresent()) {
                Officer officer = officerOpt.get();
                officer.setFullName(formOfficer.getFullName());
                officer.setEmail(formOfficer.getEmail());
                officer.setPhone(formOfficer.getPhone());
                officer.setDesignation(formOfficer.getDesignation());
                officer.setDepartment(formOfficer.getDepartment());
                officer.setAssignedArea(formOfficer.getAssignedArea());
                officer.setOfficeLocation(formOfficer.getOfficeLocation());
                officer.setAddress(formOfficer.getAddress());
                officerService.update(officer);
                redirectAttributes.addFlashAttribute("profileSuccess",
                        "Your profile information has been successfully updated.");
            }
        }
        return "redirect:/officer/profile";
    }

    // ===================================================================
    // NOTIFICATIONS
    // ===================================================================

    @GetMapping("/officer/notifications")
    public String notifications(Principal principal, Model model) {
        if (principal != null) {
            String username = principal.getName();
            List<Notification> list = notificationService.getNotificationsForOfficer(username);
            long unread = notificationService.getUnreadCountForOfficer(username);
            model.addAttribute("notificationsList", list);
            model.addAttribute("unreadCount", unread);
        } else {
            model.addAttribute("notificationsList", Collections.emptyList());
            model.addAttribute("unreadCount", 0);
        }
        return "officer/notifications";
    }

    @PostMapping("/officer/notifications/mark-all-read")
    public String markAllNotificationsRead(Principal principal, HttpServletRequest request) {
        if (principal != null) {
            notificationService.markAllAsReadForOfficer(principal.getName());
        }
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/officer/")) {
            return "redirect:" + referer;
        }
        return "redirect:/officer/notifications";
    }

    @PostMapping("/officer/notifications/{id}/read")
    public String markNotificationRead(@PathVariable("id") Long id, Principal principal, HttpServletRequest request) {
        if (principal != null) {
            notificationService.markAsReadForOfficer(id, principal.getName());
        }
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/officer/")) {
            return "redirect:" + referer;
        }
        return "redirect:/officer/notifications";
    }

    @PostMapping(value = "/api/officer/notifications/mark-all-read", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiMarkAllNotificationsRead(Principal principal) {
        if (principal != null) {
            notificationService.markAllAsReadForOfficer(principal.getName());
            return ResponseEntity.ok(Map.of("success", true, "message", "All notifications marked as read."));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated."));
    }

    @PostMapping(value = "/api/officer/notifications/{id}/read", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiMarkNotificationRead(@PathVariable("id") Long id, Principal principal) {
        if (principal != null) {
            boolean marked = notificationService.markAsReadForOfficer(id, principal.getName());
            return ResponseEntity.ok(Map.of("success", marked));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated."));
    }

    // ===================================================================
    // CHANGE PASSWORD
    // ===================================================================

    @GetMapping("/officer/change-password")
    public String changePassword(Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("officerUsername", principal.getName());
        }
        return "officer/change-password";
    }

    @PostMapping("/officer/change-password")
    public String processChangePassword(@RequestParam("currentPassword") String currentPassword,
                                        @RequestParam("newPassword") String newPassword,
                                        @RequestParam("confirmPassword") String confirmPassword,
                                        Principal principal,
                                        RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/officer/login";
        }
        try {
            officerService.changePassword(principal.getName(), currentPassword, newPassword, confirmPassword, passwordEncoder);
            redirectAttributes.addFlashAttribute("passwordSuccess", "Your password has been changed successfully. Use your new password on your next login.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("passwordError", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("passwordError", "An unexpected error occurred while updating your password. Please try again.");
        }
        return "redirect:/officer/change-password";
    }

    @PostMapping(value = "/api/officer/change-password", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiChangePassword(@RequestBody Map<String, String> payload,
                                                                 Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Authentication required."));
        }
        String currentPassword = payload.get("currentPassword");
        String newPassword = payload.get("newPassword");
        String confirmPassword = payload.get("confirmPassword");

        try {
            officerService.changePassword(principal.getName(), currentPassword, newPassword, confirmPassword, passwordEncoder);
            return ResponseEntity.ok(Map.of("success", true, "message", "Password updated successfully."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Failed to update password. Please try again."));
        }
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "O";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
