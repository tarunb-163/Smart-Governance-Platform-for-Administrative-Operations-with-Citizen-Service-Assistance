package com.civicpulse.service;

import com.civicpulse.model.Citizen;
import com.civicpulse.repository.CitizenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
        return registerCitizen(fullName, null, email, phone, rawPassword, address);
    }

    @Transactional
    public Citizen registerCitizen(String fullName, String username, String email, String phone, String rawPassword, String address) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full Name is required.");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("A valid Email address is required.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (citizenRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        // Determine username
        String normalizedUsername;
        if (username != null && !username.trim().isEmpty()) {
            normalizedUsername = username.trim().toLowerCase();
            if (citizenRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
                throw new IllegalArgumentException("An account with this username already exists.");
            }
        } else {
            String baseUsername = normalizedEmail.substring(0, normalizedEmail.indexOf('@'));
            normalizedUsername = baseUsername;
            int suffix = 1;
            while (citizenRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
                normalizedUsername = baseUsername + suffix;
                suffix++;
            }
        }

        // Generate unique Citizen ID
        long count = citizenRepository.count() + 1;
        String citizenId = String.format("CIT%05d", count);
        while (citizenRepository.existsByCitizenIdIgnoreCase(citizenId)) {
            count++;
            citizenId = String.format("CIT%05d", count);
        }

        LocalDateTime now = LocalDateTime.now();
        Citizen citizen = new Citizen();
        citizen.setCitizenId(citizenId);
        citizen.setFullName(fullName.trim());
        citizen.setUsername(normalizedUsername);
        citizen.setEmail(normalizedEmail);
        citizen.setPhone(phone != null ? phone.trim() : "");
        citizen.setPassword(passwordEncoder.encode(rawPassword));
        citizen.setAddress(address != null ? address.trim() : "");
        citizen.setCreatedAt(now);
        citizen.setLastLogin(now);
        citizen.setActive(true);

        return citizenRepository.save(citizen);
    }

    @Transactional(readOnly = true)
    public Optional<Citizen> findByIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return Optional.empty();
        }
        String search = identifier.trim();

        // 1. Match Email
        Optional<Citizen> opt = citizenRepository.findByEmailIgnoreCase(search);
        if (opt.isPresent()) {
            return opt;
        }

        // 2. Match Citizen ID
        opt = citizenRepository.findByCitizenIdIgnoreCase(search);
        if (opt.isPresent()) {
            return opt;
        }

        // 3. Match Username
        opt = citizenRepository.findByUsernameIgnoreCase(search);
        if (opt.isPresent()) {
            return opt;
        }

        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Optional<Citizen> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return Optional.empty();
        return citizenRepository.findByEmailIgnoreCase(email.trim());
    }

    @Transactional(readOnly = true)
    public Optional<Citizen> findByCitizenId(String citizenId) {
        if (citizenId == null || citizenId.trim().isEmpty()) return Optional.empty();
        return citizenRepository.findByCitizenIdIgnoreCase(citizenId.trim());
    }

    @Transactional(readOnly = true)
    public Optional<Citizen> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return Optional.empty();
        return citizenRepository.findByUsernameIgnoreCase(username.trim());
    }

    @Transactional(readOnly = true)
    public Optional<Citizen> findById(Long id) {
        if (id == null) return Optional.empty();
        return citizenRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean verifyPassword(Citizen citizen, String rawPassword) {
        if (citizen == null || rawPassword == null) return false;
        return passwordEncoder.matches(rawPassword, citizen.getPassword());
    }

    @Transactional
    public void recordLogin(Citizen citizen) {
        if (citizen != null) {
            citizen.setLastLogin(LocalDateTime.now());
            citizenRepository.save(citizen);
        }
    }

    @Transactional
    public Citizen updateProfile(String email, String fullName, String phone, String address) {
        Citizen citizen = citizenRepository.findByEmailIgnoreCase(email.trim())
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

    @Transactional(readOnly = true)
    public List<Citizen> getAllCitizens() {
        return citizenRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long getTotalCitizensCount() {
        return citizenRepository.count();
    }

    @Transactional(readOnly = true)
    public long getActiveCitizensCount() {
        return citizenRepository.countByActiveTrue();
    }

    @Transactional(readOnly = true)
    public long getInactiveCitizensCount() {
        return citizenRepository.countByActiveFalse();
    }

    @Transactional
    public Citizen toggleCitizenStatus(Long citizenId) {
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found with ID: " + citizenId));
        citizen.setActive(!citizen.isActive());
        return citizenRepository.save(citizen);
    }
}
