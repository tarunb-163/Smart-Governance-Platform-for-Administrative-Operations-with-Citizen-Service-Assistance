package com.civicpulse.controller;

import com.civicpulse.model.Citizen;
import com.civicpulse.model.Complaint;
import com.civicpulse.service.CitizenService;
import com.civicpulse.service.ComplaintService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController {

    private final ComplaintService complaintService;
    private final CitizenService citizenService;

    public AdminController(ComplaintService complaintService, CitizenService citizenService) {
        this.complaintService = complaintService;
        this.citizenService = citizenService;
    }

    // ================= ADMIN DASHBOARD =================

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {

        long total = complaintService.getTotalComplaints();
        long pending = complaintService.getPendingComplaints();
        long underReview = complaintService.getUnderReviewComplaints();
        long resolved = complaintService.getResolvedComplaints();

        // Complaint counts
        model.addAttribute("totalComplaints", total);
        model.addAttribute("pendingComplaints", pending);
        model.addAttribute("underReviewComplaints", underReview);
        model.addAttribute("resolvedComplaints", resolved);

        // Complaint percentages
        double pendingPercentage =
                total == 0 ? 0 : (pending * 100.0 / total);

        double underReviewPercentage =
                total == 0 ? 0 : (underReview * 100.0 / total);

        double resolvedPercentage =
                total == 0 ? 0 : (resolved * 100.0 / total);

        model.addAttribute("pendingPercentage", pendingPercentage);
        model.addAttribute("underReviewPercentage", underReviewPercentage);
        model.addAttribute("resolvedPercentage", resolvedPercentage);

        // Recent complaints
        model.addAttribute("recentComplaints", complaintService.getRecentComplaints());

        return "admin/dashboard";
    }

    // ================= COMPLAINTS =================

    @GetMapping("/admin/complaints")
    public String complaints(Model model) {
        model.addAttribute("complaints", complaintService.getAllComplaints());
        return "admin/complaints";
    }

    // ================= COMPLAINT DETAILS =================

    @GetMapping("/admin/complaints/{id}")
    public String complaintDetails(@PathVariable Long id, Model model) {
        model.addAttribute("complaint", complaintService.getComplaintById(id));
        return "admin/complaint-details";
    }

    // ================= UPDATE STATUS =================

    @PostMapping("/admin/complaints/{id}/status")
    public String updateComplaintStatus(@PathVariable Long id, @RequestParam String status) {
        Complaint complaint = complaintService.getComplaintById(id);

        if (complaint != null) {
            complaint.setStatus(status);
            complaintService.saveComplaint(complaint);
        }

        return "redirect:/admin/complaints/" + id;
    }

    // ================= OTHER ADMIN PAGES =================

    @GetMapping("/admin/officers")
    public String officers() {
        return "admin/officers";
    }

    @GetMapping("/admin/citizens")
    public String citizens(Model model) {
        List<Citizen> citizens = citizenService.getAllCitizens();
        model.addAttribute("citizens", citizens);
        model.addAttribute("totalCitizens", citizenService.getTotalCitizensCount());
        model.addAttribute("activeCitizens", citizenService.getActiveCitizensCount());
        model.addAttribute("inactiveCitizens", citizenService.getInactiveCitizensCount());
        return "admin/citizens";
    }

    @PostMapping("/admin/citizens/{id}/toggle-status")
    public String toggleCitizenStatus(@PathVariable Long id) {
        citizenService.toggleCitizenStatus(id);
        return "redirect:/admin/citizens";
    }

    @GetMapping("/admin/reports")
    public String reports(Model model) {
        model.addAttribute("totalComplaints", complaintService.getTotalComplaints());
        model.addAttribute("pendingComplaints", complaintService.getPendingComplaints());
        model.addAttribute("underReviewComplaints", complaintService.getUnderReviewComplaints());
        model.addAttribute("resolvedComplaints", complaintService.getResolvedComplaints());
        return "admin/reports";
    }

    @GetMapping("/admin/profile")
    public String profile() {
        return "admin/profile";
    }

    @GetMapping("/admin/settings")
    public String settings() {
        return "admin/settings";
    }
}