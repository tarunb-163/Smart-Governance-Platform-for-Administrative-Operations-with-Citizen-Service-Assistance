package com.civicpulse.controller.api;

import com.civicpulse.model.Officer;
import com.civicpulse.service.OfficerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/officer/profile")
public class OfficerProfileApiController {

    private final OfficerService officerService;

    @Autowired
    public OfficerProfileApiController(OfficerService officerService) {
        this.officerService = officerService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfile(Principal principal) {
        Officer officer = null;
        if (principal != null) {
            officer = officerService.findByUsername(principal.getName()).orElse(null);
        }
        if (officer == null) {
            officer = officerService.findByUsername("officer1").orElse(null);
        }

        if (officer == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapOfficerToResponse(officer));
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> body, Principal principal) {
        Officer officer = null;
        if (principal != null) {
            officer = officerService.findByUsername(principal.getName()).orElse(null);
        }
        if (officer == null) {
            officer = officerService.findByUsername("officer1").orElse(null);
        }

        if (officer == null) {
            return ResponseEntity.notFound().build();
        }

        if (body.containsKey("fullName") && body.get("fullName") != null && !body.get("fullName").trim().isEmpty()) {
            officer.setFullName(body.get("fullName").trim());
        }
        if (body.containsKey("email") && body.get("email") != null && !body.get("email").trim().isEmpty()) {
            officer.setEmail(body.get("email").trim());
        }
        if (body.containsKey("phone") && body.get("phone") != null && !body.get("phone").trim().isEmpty()) {
            officer.setPhone(body.get("phone").trim());
        }
        if (body.containsKey("designation") && body.get("designation") != null && !body.get("designation").trim().isEmpty()) {
            officer.setDesignation(body.get("designation").trim());
        }
        if (body.containsKey("department") && body.get("department") != null && !body.get("department").trim().isEmpty()) {
            officer.setDepartment(body.get("department").trim());
        }
        if (body.containsKey("assignedArea") && body.get("assignedArea") != null && !body.get("assignedArea").trim().isEmpty()) {
            officer.setAssignedArea(body.get("assignedArea").trim());
        }
        if (body.containsKey("officeLocation") && body.get("officeLocation") != null && !body.get("officeLocation").trim().isEmpty()) {
            officer.setOfficeLocation(body.get("officeLocation").trim());
        }
        if (body.containsKey("address") && body.get("address") != null && !body.get("address").trim().isEmpty()) {
            officer.setAddress(body.get("address").trim());
        }

        officerService.update(officer);

        Map<String, Object> response = mapOfficerToResponse(officer);
        response.put("success", true);
        response.put("message", "Profile updated successfully.");
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> mapOfficerToResponse(Officer officer) {
        Map<String, Object> map = new HashMap<>();
        map.put("fullName", officer.getFullName() != null ? officer.getFullName() : "Officer One");
        map.put("employeeId", officer.getEmployeeId() != null ? officer.getEmployeeId() : "EMP001");
        map.put("email", officer.getEmail() != null ? officer.getEmail() : "officer1@civicpulse.com");
        map.put("phone", officer.getPhone() != null ? officer.getPhone() : "+91 9876543210");
        map.put("designation", officer.getDesignation() != null ? officer.getDesignation() : "Senior Field Officer");
        map.put("department", officer.getDepartment() != null ? officer.getDepartment() : "Roads / Public Works Department");
        map.put("assignedArea", officer.getAssignedArea() != null ? officer.getAssignedArea() : "Central Ward & Sector 4");
        map.put("officeLocation", officer.getOfficeLocation() != null ? officer.getOfficeLocation() : "Municipal PWD Headquarters, Zone 1");
        map.put("address", officer.getAddress() != null ? officer.getAddress() : "Room 204, PWD Administrative Block, Civic Center, Andhra Pradesh");
        map.put("role", officer.getRole() != null ? officer.getRole() : "OFFICER");
        map.put("jurisdiction", officer.getJurisdiction() != null ? officer.getJurisdiction() : "Municipal North & Central Zones");
        map.put("joinedDate", officer.getJoinedDate() != null ? officer.getJoinedDate().toString() : "2026-03-16");
        return map;
    }
}
