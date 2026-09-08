package com.civicpulse.controller;

import com.civicpulse.model.Citizen;
import com.civicpulse.model.Complaint;
import com.civicpulse.model.Notification;
import com.civicpulse.service.CitizenService;
import com.civicpulse.service.ComplaintService;
import com.civicpulse.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
public class CitizenController {

    private final ComplaintService complaintService;
    private final CitizenService citizenService;
    private final NotificationService notificationService;

    public CitizenController(ComplaintService complaintService,
                             CitizenService citizenService,
                             NotificationService notificationService) {
        this.complaintService = complaintService;
        this.citizenService = citizenService;
        this.notificationService = notificationService;
    }

    private String getAuthenticatedEmail(HttpSession session) {
        String email = (String) session.getAttribute("CITIZEN_EMAIL");
        if (email != null && !email.trim().isEmpty()) {
            return email;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String principal = auth.getName();
            Optional<Citizen> opt = citizenService.findByIdentifier(principal);
            if (opt.isPresent()) {
                Citizen c = opt.get();
                session.setAttribute("CITIZEN_EMAIL", c.getEmail());
                session.setAttribute("CITIZEN_NAME", c.getFullName());
                session.setAttribute("CITIZEN_ID", c.getCitizenId());
                if (c.getUsername() != null) {
                    session.setAttribute("CITIZEN_USERNAME", c.getUsername());
                }
                return c.getEmail();
            }
        }

        return null;
    }

    @GetMapping("/citizen/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String email = getAuthenticatedEmail(session);
        if (email == null) {
            return "redirect:/citizen/login";
        }

        Optional<Citizen> citizenOpt = citizenService.findByEmail(email);
        Citizen citizen = citizenOpt.orElse(null);

        model.addAttribute("citizen", citizen);
        model.addAttribute("citizenName", citizen != null ? citizen.getFullName() : session.getAttribute("CITIZEN_NAME"));
        model.addAttribute("citizenEmail", email);

        // Dynamic current date
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"));
        model.addAttribute("currentDate", currentDate);

        // Citizen-specific statistics
        long total = complaintService.getTotalComplaintsForCitizen(email);
        long pending = complaintService.getPendingComplaintsForCitizen(email);
        long inProgress = complaintService.getInProgressComplaintsForCitizen(email);
        long resolved = complaintService.getResolvedComplaintsForCitizen(email);

        model.addAttribute("totalComplaints", total);
        model.addAttribute("pendingComplaints", pending);
        model.addAttribute("inProgressComplaints", inProgress);
        model.addAttribute("resolvedComplaints", resolved);
        model.addAttribute("activeComplaints", pending + inProgress);

        // Recent complaints and notifications
        List<Complaint> recent = complaintService.getRecentComplaintsForCitizen(email, 5);
        model.addAttribute("recentComplaints", recent);

        List<Notification> notifications = notificationService.getNotificationsForCitizen(email);
        model.addAttribute("recentNotifications", notifications.stream().limit(5).toList());
        model.addAttribute("unreadNotificationsCount", notificationService.getUnreadCount(email));

        return "citizen/dashboard";
    }

    @GetMapping("/citizen/complaint-form")
    public String complaintForm(HttpSession session, Model model) {
        String email = getAuthenticatedEmail(session);
        if (email == null) {
            return "redirect:/citizen/login";
        }

        Optional<Citizen> citizenOpt = citizenService.findByEmail(email);
        model.addAttribute("citizen", citizenOpt.orElse(null));
        model.addAttribute("citizenName", session.getAttribute("CITIZEN_NAME"));
        return "citizen/complaint-form";
    }

    @GetMapping("/citizen/complaints")
    public String complaints(HttpSession session, Model model) {
        String email = getAuthenticatedEmail(session);
        if (email == null) {
            return "redirect:/citizen/login";
        }

        List<Complaint> citizenComplaints = complaintService.getComplaintsForCitizen(email);
        model.addAttribute("complaints", citizenComplaints);
        model.addAttribute("citizenName", session.getAttribute("CITIZEN_NAME"));
        return "citizen/complaints";
    }

    @GetMapping("/citizen/track")
    public String trackComplaint(HttpSession session, Model model) {
        String email = getAuthenticatedEmail(session);
        if (email == null) {
            return "redirect:/citizen/login";
        }
        model.addAttribute("citizenName", session.getAttribute("CITIZEN_NAME"));
        return "citizen/track";
    }

    @GetMapping("/citizen/notifications")
    public String notifications(HttpSession session, Model model) {
        String email = getAuthenticatedEmail(session);
        if (email == null) {
            return "redirect:/citizen/login";
        }

        List<Notification> list = notificationService.getNotificationsForCitizen(email);
        model.addAttribute("notifications", list);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(email));
        model.addAttribute("citizenName", session.getAttribute("CITIZEN_NAME"));
        return "citizen/notifications";
    }

    @GetMapping("/citizen/profile")
    public String profile(HttpSession session, Model model) {
        String email = getAuthenticatedEmail(session);
        if (email == null) {
            return "redirect:/citizen/login";
        }

        Optional<Citizen> citizenOpt = citizenService.findByEmail(email);
        Citizen citizen = citizenOpt.orElseGet(() -> {
            Citizen c = new Citizen();
            c.setFullName((String) session.getAttribute("CITIZEN_NAME"));
            c.setEmail(email);
            c.setCitizenId((String) session.getAttribute("CITIZEN_ID"));
            return c;
        });

        model.addAttribute("citizen", citizen);
        model.addAttribute("citizenName", citizen.getFullName());
        return "citizen/profile";
    }
}