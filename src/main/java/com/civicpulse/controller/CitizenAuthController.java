package com.civicpulse.controller;

import com.civicpulse.model.Citizen;
import com.civicpulse.service.CitizenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Controller
public class CitizenAuthController {

    private final CitizenService citizenService;

    public CitizenAuthController(CitizenService citizenService) {
        this.citizenService = citizenService;
    }

    // ==========================================
    // VIEW ROUTES
    // ==========================================

    @GetMapping("/citizen/login")
    public String citizenLoginPage(HttpSession session, Model model,
                                   @RequestParam(value = "logout", required = false) String logout,
                                   @RequestParam(value = "registered", required = false) String registered) {
        if (session.getAttribute("CITIZEN_EMAIL") != null) {
            return "redirect:/citizen/dashboard";
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Account created successfully! Please log in.");
        }
        return "citizen/login";
    }

    @GetMapping({"/citizen/register", "/citizen/signup"})
    public String citizenRegisterPage(HttpSession session) {
        if (session.getAttribute("CITIZEN_EMAIL") != null) {
            return "redirect:/citizen/dashboard";
        }
        return "citizen/register";
    }

    @GetMapping("/citizen/logout")
    public String citizenLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return "redirect:/citizen/login?logout";
    }

    // ==========================================
    // REST API ENDPOINTS
    // ==========================================

    @PostMapping("/api/citizen/auth/register")
    @ResponseBody
    public ResponseEntity<?> registerCitizen(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String fullName = body.get("fullName");
        String username = body.get("username");
        String email = body.get("email");
        String phone = body.get("phone");
        String password = body.get("password");
        String confirmPassword = body.get("confirmPassword");
        String address = body.get("address");

        if (fullName == null || fullName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Full Name is required."));
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "A valid email address is required."));
        }
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters long."));
        }
        if (confirmPassword != null && !password.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Passwords do not match."));
        }

        try {
            Citizen citizen = citizenService.registerCitizen(fullName, username, email, phone, password, address);

            // Establish authenticated session
            HttpSession session = request.getSession(true);
            session.setAttribute("CITIZEN_EMAIL", citizen.getEmail());
            session.setAttribute("CITIZEN_NAME", citizen.getFullName());
            session.setAttribute("CITIZEN_ID", citizen.getCitizenId());
            if (citizen.getUsername() != null) {
                session.setAttribute("CITIZEN_USERNAME", citizen.getUsername());
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    citizen.getEmail(), null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_CITIZEN"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Account registered successfully!",
                    "citizenId", citizen.getCitizenId(),
                    "username", citizen.getUsername() != null ? citizen.getUsername() : "",
                    "redirectUrl", "/citizen/dashboard"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/api/citizen/auth/login")
    @ResponseBody
    public ResponseEntity<?> loginCitizen(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String identifier = body.get("identifier");
        if (identifier == null || identifier.trim().isEmpty()) {
            identifier = body.get("username");
        }
        if (identifier == null || identifier.trim().isEmpty()) {
            identifier = body.get("email");
        }
        String password = body.get("password");

        if (identifier == null || identifier.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email/Username/Citizen ID and Password are required."));
        }

        Optional<Citizen> citizenOpt = citizenService.findByIdentifier(identifier);

        if (citizenOpt.isEmpty() || !citizenService.verifyPassword(citizenOpt.get(), password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email/username or password."));
        }

        Citizen citizen = citizenOpt.get();
        if (!citizen.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Your account is deactivated. Please contact support."));
        }

        // Record activity timestamp
        citizenService.recordLogin(citizen);

        HttpSession session = request.getSession(true);
        session.setAttribute("CITIZEN_EMAIL", citizen.getEmail());
        session.setAttribute("CITIZEN_NAME", citizen.getFullName());
        session.setAttribute("CITIZEN_ID", citizen.getCitizenId());
        if (citizen.getUsername() != null) {
            session.setAttribute("CITIZEN_USERNAME", citizen.getUsername());
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(
                citizen.getEmail(), null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_CITIZEN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login successful.",
                "redirectUrl", "/citizen/dashboard"
        ));
    }

    @GetMapping("/api/citizen/auth/me")
    @ResponseBody
    public ResponseEntity<?> getCurrentCitizen(HttpSession session) {
        String email = (String) session.getAttribute("CITIZEN_EMAIL");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        Optional<Citizen> citizenOpt = citizenService.findByEmail(email);
        if (citizenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Citizen not found"));
        }
        Citizen citizen = citizenOpt.get();
        return ResponseEntity.ok(Map.of(
                "citizenId", citizen.getCitizenId(),
                "fullName", citizen.getFullName(),
                "username", citizen.getUsername() != null ? citizen.getUsername() : "",
                "email", citizen.getEmail(),
                "phone", citizen.getPhone() != null ? citizen.getPhone() : "",
                "address", citizen.getAddress() != null ? citizen.getAddress() : "",
                "lastLogin", citizen.getLastLogin() != null ? citizen.getLastLogin().toString() : ""
        ));
    }

    @PostMapping("/api/citizen/profile")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, HttpSession session) {
        String email = (String) session.getAttribute("CITIZEN_EMAIL");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        String fullName = body.get("fullName");
        String phone = body.get("phone");
        String address = body.get("address");

        try {
            Citizen updated = citizenService.updateProfile(email, fullName, phone, address);
            session.setAttribute("CITIZEN_NAME", updated.getFullName());
            return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
