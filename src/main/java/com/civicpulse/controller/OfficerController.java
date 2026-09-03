package com.civicpulse.controller;

import com.civicpulse.service.ComplaintService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OfficerController {

    private final ComplaintService complaintService;

    public OfficerController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalComplaints", complaintService.getTotalComplaints());
        model.addAttribute("pendingComplaints", complaintService.getPendingComplaints());
        model.addAttribute("resolvedComplaints", complaintService.getResolvedComplaints());
        return "portal";
    }

    @GetMapping("/officer/login")
    public String officerLogin() {
        return "login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
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
    public String complaintDetails() {
        return "officer/complaint-details";
    }

    @GetMapping("/officer/update-complaint")
    public String updateComplaint() {
        return "officer/update-complaint";
    }

    @GetMapping("/officer/profile")
    public String profile() {
        return "officer/profile";
    }

    @GetMapping("/officer/change-password")
    public String changePassword() {
        return "officer/change-password";
    }
}