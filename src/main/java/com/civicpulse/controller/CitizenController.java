package com.civicpulse.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CitizenController {

    @GetMapping("/citizen/dashboard")
    public String dashboard() {
        return "citizen/dashboard";
    }

    @GetMapping("/citizen/complaint-form")
    public String complaintForm() {
        return "citizen/complaint-form";
    }

    @GetMapping("/citizen/complaints")
    public String complaints() {
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