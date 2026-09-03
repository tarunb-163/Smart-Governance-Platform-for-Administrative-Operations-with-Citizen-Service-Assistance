package com.civicpulse.controller;

import com.civicpulse.service.ComplaintService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CitizenController {

    private final ComplaintService complaintService;

    public CitizenController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/citizen/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalComplaints", complaintService.getTotalComplaints());
        model.addAttribute("pendingComplaints", complaintService.getPendingComplaints());
        model.addAttribute("resolvedComplaints", complaintService.getResolvedComplaints());
        model.addAttribute("recentComplaints", complaintService.getRecentComplaints());
        return "citizen/dashboard";
    }

    @GetMapping("/citizen/complaint-form")
    public String complaintForm() {
        return "citizen/complaint-form";
    }

    @GetMapping("/citizen/complaints")
    public String complaints(Model model) {
        model.addAttribute("complaints", complaintService.getAllComplaints());
        return "citizen/complaints";
    }

    @GetMapping("/citizen/track")
    public String trackComplaint() {
        return "citizen/track";
    }

    @GetMapping("/citizen/notifications")
    public String notifications() {
        return "citizen/notifications";
    }

    @GetMapping("/citizen/profile")
    public String profile() {
        return "citizen/profile";
    }
}