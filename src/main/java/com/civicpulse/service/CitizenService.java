package com.civicpulse.service;

import com.civicpulse.model.Citizen;
import com.civicpulse.repository.CitizenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CitizenService {

    private final CitizenRepository citizenRepository;
    private final PasswordEncoder passwordEncoder;

    public CitizenService(CitizenRepository citizenRepository, PasswordEncoder passwordEncoder) {
        this.citizenRepository = citizenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Citizen registerCitizen(String fullName, String email, String phone, String rawPassword, String address) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full Name is required.");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("A valid Email address is required.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (citizenRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        long count = citizenRepository.count() + 1;
        String citizenId = String.format("CIT%05d", count);

        Citizen citizen = new Citizen();
        citizen.setCitizenId(citizenId);
        citizen.setFullName(fullName.trim());
        citizen.setEmail(email.trim().toLowerCase());
        citizen.setPhone(phone != null ? phone.trim() : "");
        citizen.setPassword(passwordEncoder.encode(rawPassword));
        citizen.setAddress(address != null ? address.trim() : "");
        citizen.setCreatedAt(LocalDateTime.now());
        citizen.setActive(true);

        return citizenRepository.save(citizen);
    }

    @Transactional(readOnly = true)
    public Optional<Citizen> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return citizenRepository.findByEmail(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public Optional<Citizen> findByCitizenId(String citizenId) {
        if (citizenId == null) return Optional.empty();
        return citizenRepository.findByCitizenId(citizenId.trim());
    }

    @Transactional(readOnly = true)
    public boolean verifyPassword(Citizen citizen, String rawPassword) {
        if (citizen == null || rawPassword == null) return false;
        return passwordEncoder.matches(rawPassword, citizen.getPassword());
    }

    @Transactional
    public Citizen updateProfile(String email, String fullName, String phone, String address) {
        Citizen citizen = citizenRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found with email: " + email));

        if (fullName != null && !fullName.trim().isEmpty()) {
            citizen.setFullName(fullName.trim());
        }
        if (phone != null) {
            citizen.setPhone(phone.trim());
        }
        if (address != null) {
            citizen.setAddress(address.trim());
        }

        return citizenRepository.save(citizen);
    }
}
