package com.civicpulse.controller;

import com.civicpulse.entites.Admin;
import com.civicpulse.entites.Citizen;
import com.civicpulse.entites.Complaint;
import com.civicpulse.entites.Officer;
import com.civicpulse.entites.SystemSetting;
import com.civicpulse.repositories.AdminRepository;
import com.civicpulse.repositories.CitizenRepository;
import com.civicpulse.repositories.OfficerRepository;
import com.civicpulse.repositories.SystemSettingRepository;
import com.civicpulse.services.ComplaintService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    private final ComplaintService complaintService;
    private final OfficerRepository officerRepository;
    private final CitizenRepository citizenRepository;
    private final AdminRepository adminRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(ComplaintService complaintService,
                           OfficerRepository officerRepository,
                           CitizenRepository citizenRepository,
                           AdminRepository adminRepository,
                           SystemSettingRepository systemSettingRepository,
                           PasswordEncoder passwordEncoder) {
        this.complaintService = complaintService;
        this.officerRepository = officerRepository;
        this.citizenRepository = citizenRepository;
        this.adminRepository = adminRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        List<Complaint> complaints = complaintService.getAllComplaints();
        addComplaintStats(model, complaints);
        model.addAttribute("recentComplaints", complaintService.getRecentComplaints());
        model.addAttribute("totalOfficers", officerRepository.count());
        model.addAttribute("activeOfficers", officerRepository.countByActiveTrue());
        model.addAttribute("totalCitizens", citizenRepository.count());
        return "admin/dashboard";
    }

    @GetMapping("/admin/complaints")
    public String complaints(Model model) {
        List<Complaint> complaints = complaintService.getAllComplaints();
        addComplaintStats(model, complaints);
        model.addAttribute("complaints", complaints);
        model.addAttribute("officers", officerRepository.findAll());
        return "admin/complaints";
    }

    @GetMapping("/admin/complaints/{id}")
    public String complaintDetails(@PathVariable Long id, Model model) {
        model.addAttribute("complaint", complaintService.getComplaintById(id));
        model.addAttribute("officers", officerRepository.findAll());
        return "admin/complaint-details";
    }

    @PostMapping("/admin/complaints/{id}/status")
    public String updateComplaintStatus(@PathVariable Long id,
                                        @RequestParam String status,
                                        RedirectAttributes redirectAttributes) {
        Complaint complaint = complaintService.getComplaintById(id);
        if (complaint == null) {
            redirectAttributes.addFlashAttribute("error", "Complaint not found.");
        } else {
            complaint.setStatus(status);
            complaintService.saveComplaint(complaint);
            redirectAttributes.addFlashAttribute("success", "Complaint status updated successfully.");
        }
        return "redirect:/admin/complaints/" + id;
    }

    @PostMapping("/admin/complaints/{id}/progress")
    public String updateComplaintProgress(@PathVariable Long id,
                                          @RequestParam(required = false) String progressUpdate,
                                          RedirectAttributes redirectAttributes) {
        Complaint complaint = complaintService.getComplaintById(id);
        if (complaint == null) {
            redirectAttributes.addFlashAttribute("error", "Complaint not found.");
        } else {
            complaint.setProgressUpdate(progressUpdate == null ? null : progressUpdate.trim());
            complaintService.saveComplaint(complaint);
            redirectAttributes.addFlashAttribute("success", "Work progress update saved successfully.");
        }
        return "redirect:/admin/complaints/" + id;
    }

    @PostMapping("/admin/complaints/{id}/assign")
    public String assignComplaint(@PathVariable Long id,
                                  @RequestParam Long officerId,
                                  RedirectAttributes redirectAttributes) {
        Complaint complaint = complaintService.getComplaintById(id);
        Officer officer = officerRepository.findById(officerId).orElse(null);
        if (complaint == null || officer == null) {
            redirectAttributes.addFlashAttribute("error", "Complaint or officer not found.");
            return "redirect:/admin/complaints/" + id;
        }
        complaint.setAssignedOfficer(officer);
        complaint.setAssignedAt(java.time.LocalDateTime.now());
        complaintService.saveComplaint(complaint);
        redirectAttributes.addFlashAttribute("success", "Complaint assigned to " + officer.getFullName() + ".");
        return "redirect:/admin/complaints/" + id;
    }

    @GetMapping("/admin/officers")
    public String officers(@RequestParam(required = false) Long editId,
                           Model model) {
        model.addAttribute("officers", officerRepository.findAll());
        model.addAttribute("totalOfficers", officerRepository.count());
        model.addAttribute("activeOfficers", officerRepository.countByActiveTrue());
        model.addAttribute("inactiveOfficers", officerRepository.countByActiveFalse());
        Officer formOfficer = editId == null ? new Officer() : officerRepository.findById(editId).orElse(new Officer());
        model.addAttribute("formOfficer", formOfficer);
        model.addAttribute("editing", editId != null && formOfficer.getId() != null);
        return "admin/officers";
    }

    @PostMapping("/admin/officers/save")
    public String saveOfficer(@RequestParam(required = false) Long id,
                              @RequestParam String username,
                              @RequestParam String fullName,
                              @RequestParam(required = false) String employeeId,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String phone,
                              @RequestParam(required = false) String designation,
                              @RequestParam(required = false) String department,
                              @RequestParam(required = false) String assignedArea,
                              @RequestParam(required = false) String officeLocation,
                              @RequestParam(required = false) String address,
                              @RequestParam(required = false) String jurisdiction,
                              @RequestParam(required = false) String role,
                              @RequestParam(required = false) String password,
                              @RequestParam(defaultValue = "false") boolean active,
                              RedirectAttributes redirectAttributes) {
        Officer officer = id == null ? new Officer() : officerRepository.findById(id).orElse(null);
        if (officer == null) {
            redirectAttributes.addFlashAttribute("error", "Officer not found.");
            return "redirect:/admin/officers";
        }
        if (id == null && officerRepository.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Username already exists.");
            return "redirect:/admin/officers";
        }
        if (employeeId != null && !employeeId.isBlank() && (id == null || !employeeId.equals(officer.getEmployeeId()))
                && officerRepository.existsByEmployeeId(employeeId)) {
            redirectAttributes.addFlashAttribute("error", "Employee ID already exists.");
            return "redirect:/admin/officers";
        }
        officer.setUsername(username.trim());
        officer.setFullName(fullName.trim());
        officer.setEmployeeId(blankToNull(employeeId));
        officer.setEmail(blankToNull(email));
        officer.setPhone(blankToNull(phone));
        officer.setDesignation(blankToNull(designation));
        officer.setDepartment(blankToNull(department));
        officer.setAssignedArea(blankToNull(assignedArea));
        officer.setOfficeLocation(blankToNull(officeLocation));
        officer.setAddress(blankToNull(address));
        officer.setJurisdiction(blankToNull(jurisdiction));
        officer.setRole(role == null || role.isBlank() ? "OFFICER" : role.toUpperCase());
        officer.setActive(active);
        if (id == null) {
            officer.setPassword(password == null || password.isBlank()
                    ? passwordEncoder.encode("password123")
                    : passwordEncoder.encode(password));
            officer.setJoinedDate(LocalDate.now());
        } else if (password != null && !password.isBlank()) {
            officer.setPassword(passwordEncoder.encode(password));
        }
        officerRepository.save(officer);
        redirectAttributes.addFlashAttribute("success", id == null ? "Officer added successfully." : "Officer updated successfully.");
        return "redirect:/admin/officers";
    }

    @PostMapping("/admin/officers/{id}/delete")
    public String deleteOfficer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Officer officer = officerRepository.findById(id).orElse(null);
        if (officer == null) {
            redirectAttributes.addFlashAttribute("error", "Officer not found.");
            return "redirect:/admin/officers";
        }
        List<Complaint> assigned = complaintService.getAllComplaints().stream()
                .filter(c -> c.getAssignedOfficer() != null && id.equals(c.getAssignedOfficer().getId()))
                .collect(Collectors.toList());
        assigned.forEach(c -> { c.setAssignedOfficer(null); complaintService.saveComplaint(c); });
        officerRepository.delete(officer);
        redirectAttributes.addFlashAttribute("success", "Officer deleted successfully.");
        return "redirect:/admin/officers";
    }

    @GetMapping("/admin/citizens")
    public String citizens(Model model) {
        List<Citizen> citizens = citizenRepository.findAll();
        model.addAttribute("citizens", citizens);
        model.addAttribute("totalCitizens", citizens.size());
        return "admin/citizens";
    }

    @GetMapping("/admin/reports")
    public String reports(@RequestParam(required = false) String status, Model model) {
        List<Complaint> all = complaintService.getAllComplaints();
        List<Complaint> filtered = filterByStatus(all, status);
        addComplaintStats(model, filtered);
        model.addAttribute("complaints", filtered);
        model.addAttribute("categoryCounts", groupCounts(filtered, true));
        model.addAttribute("locationCounts", groupCounts(filtered, false));
        model.addAttribute("recentComplaints", all.stream().sorted(Comparator.comparing(Complaint::getId, Comparator.nullsLast(Comparator.reverseOrder()))).limit(10).toList());
        model.addAttribute("selectedStatus", status == null || status.isBlank() ? "ALL" : status);
        return "admin/reports";
    }

    @GetMapping({"/admin/reports/download/csv", "/admin/reports/download-csv"})
    public ResponseEntity<byte[]> downloadCsvReport(@RequestParam(required = false) String status) {
        List<Complaint> complaints = filterByStatus(complaintService.getAllComplaints(), status);
        StringBuilder csv = new StringBuilder("ID,Complaint Number,Category,Title,Citizen,Email,Department,Location,Priority,Status,Assigned Officer,Created At\n");
        for (Complaint c : complaints) {
            csv.append(csv(c.getId())).append(',').append(csv(c.getComplaintNumber())).append(',')
                    .append(csv(c.getCategory())).append(',').append(csv(c.getTitle())).append(',')
                    .append(csv(c.getCitizenName())).append(',').append(csv(c.getCitizenEmail())).append(',')
                    .append(csv(c.getDepartment())).append(',').append(csv(c.getLocation())).append(',')
                    .append(csv(c.getPriority())).append(',').append(csv(c.getStatus())).append(',')
                    .append(csv(c.getAssignedOfficer() == null ? null : c.getAssignedOfficer().getUsername())).append(',')
                    .append(csv(c.getCreatedAt())).append('\n');
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=civicpulse-complaint-report.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8")).body(bytes);
    }

    @GetMapping("/admin/settings")
    public String settings(Model model) {
        SystemSetting settings = systemSettingRepository.findAll().stream().findFirst().orElseGet(SystemSetting::new);
        model.addAttribute("settings", settings);
        return "admin/settings";
    }

    @PostMapping("/admin/settings")
    public String saveSettings(@RequestParam String applicationName,
                               @RequestParam String defaultComplaintStatus,
                               RedirectAttributes redirectAttributes) {
        SystemSetting settings = systemSettingRepository.findAll().stream().findFirst().orElseGet(SystemSetting::new);
        settings.setApplicationName(applicationName == null || applicationName.isBlank() ? "CivicPulse" : applicationName.trim());
        settings.setDefaultComplaintStatus(defaultComplaintStatus);
        systemSettingRepository.save(settings);
        redirectAttributes.addFlashAttribute("success", "Settings saved successfully.");
        return "redirect:/admin/settings";
    }

    @GetMapping("/admin/profile")
    public String profile(Authentication authentication, Model model) {
        Admin admin = getCurrentAdmin(authentication);
        model.addAttribute("adminName", admin == null ? "Administrator" : admin.getFullName());
        model.addAttribute("adminRole", admin == null ? "System Administrator" : admin.getRole());
        model.addAttribute("accountType", "Administrator Account");
        model.addAttribute("adminEmail", admin == null ? "" : admin.getEmail());
        return "admin/profile";
    }

    @PostMapping("/admin/profile")
    public String updateProfile(Authentication authentication,
                                @RequestParam String displayName,
                                @RequestParam String email,
                                RedirectAttributes redirectAttributes) {
        Admin admin = getCurrentAdmin(authentication);
        if (admin == null) {
            redirectAttributes.addFlashAttribute("error", "Admin account not found.");
            return "redirect:/admin/profile";
        }
        if (email == null || email.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Email is required.");
            return "redirect:/admin/profile";
        }
        admin.setFullName(displayName == null || displayName.isBlank() ? admin.getFullName() : displayName.trim());
        admin.setEmail(email.trim());
        adminRepository.save(admin);
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
        return "redirect:/admin/profile";
    }

    private Admin getCurrentAdmin(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return null;
        return adminRepository.findByUsername(authentication.getName()).orElse(null);
    }

    private void addComplaintStats(Model model, List<Complaint> complaints) {
        long total = complaints.size();
        long pending = countStatus(complaints, "PENDING");
        long underReview = countStatus(complaints, "UNDER REVIEW", "UNDER_REVIEW");
        long inProgress = countStatus(complaints, "IN PROGRESS", "IN_PROGRESS");
        long resolved = countStatus(complaints, "RESOLVED");
        long rejected = countStatus(complaints, "REJECTED");
        model.addAttribute("totalComplaints", total);
        model.addAttribute("pendingComplaints", pending);
        model.addAttribute("underReviewComplaints", underReview);
        model.addAttribute("inProgressComplaints", inProgress);
        model.addAttribute("resolvedComplaints", resolved);
        model.addAttribute("rejectedComplaints", rejected);
        model.addAttribute("pendingPercentage", percentage(pending, total));
        model.addAttribute("underReviewPercentage", percentage(underReview, total));
        model.addAttribute("inProgressPercentage", percentage(inProgress, total));
        model.addAttribute("resolvedPercentage", percentage(resolved, total));
        model.addAttribute("rejectedPercentage", percentage(rejected, total));
    }

    private long countStatus(List<Complaint> list, String... statuses) {
        return list.stream().filter(c -> c.getStatus() != null)
                .filter(c -> java.util.Arrays.stream(statuses).anyMatch(s -> s.equalsIgnoreCase(c.getStatus()))).count();
    }

    private double percentage(long value, long total) { return total == 0 ? 0 : value * 100.0 / total; }

    private List<Complaint> filterByStatus(List<Complaint> all, String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) return all;
        return all.stream().filter(c -> c.getStatus() != null && normalizeStatus(c.getStatus()).equals(normalizeStatus(status))).toList();
    }

    private String normalizeStatus(String value) { return value.trim().replace('_', ' ').replace('-', ' ').replaceAll("\\s+", " ").toUpperCase(); }

    private Map<String, Long> groupCounts(List<Complaint> complaints, boolean category) {
        return complaints.stream().map(c -> category ? c.getCategory() : c.getLocation()).filter(v -> v != null && !v.isBlank())
                .collect(Collectors.groupingBy(v -> v, LinkedHashMap::new, Collectors.counting())).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a,b)->a, LinkedHashMap::new));
    }

    private String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
    private String csv(Object value) { if (value == null) return "\"\""; String s = String.valueOf(value).replace("\"", "\"\""); return "\"" + s + "\""; }
}
