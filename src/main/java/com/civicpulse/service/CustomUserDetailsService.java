package com.civicpulse.service;

import com.civicpulse.model.Citizen;
import com.civicpulse.model.Officer;
import com.civicpulse.repository.CitizenRepository;
import com.civicpulse.repository.OfficerRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final OfficerRepository officerRepository;
    private final CitizenRepository citizenRepository;

    public CustomUserDetailsService(OfficerRepository officerRepository, CitizenRepository citizenRepository) {
        this.officerRepository = officerRepository;
        this.citizenRepository = citizenRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.trim().isEmpty()) {
            throw new UsernameNotFoundException("Username/Identifier cannot be empty");
        }
        String cleanIdentifier = username.trim();

        // 1. Try Officer lookup
        Optional<Officer> officerOpt = officerRepository.findByUsername(cleanIdentifier);
        if (officerOpt.isPresent()) {
            Officer officer = officerOpt.get();
            return new User(
                    officer.getUsername(),
                    officer.getPassword(),
                    officer.isActive(),
                    true,
                    true,
                    true,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + (officer.getRole() != null ? officer.getRole() : "OFFICER")))
            );
        }

        // 2. Try Citizen lookup by Email, Citizen ID, or Username (case-insensitive)
        Optional<Citizen> citizenOpt = citizenRepository.findByEmailIgnoreCase(cleanIdentifier);
        if (citizenOpt.isEmpty()) {
            citizenOpt = citizenRepository.findByCitizenIdIgnoreCase(cleanIdentifier);
        }
        if (citizenOpt.isEmpty()) {
            citizenOpt = citizenRepository.findByUsernameIgnoreCase(cleanIdentifier);
        }

        if (citizenOpt.isPresent()) {
            Citizen citizen = citizenOpt.get();
            return new User(
                    citizen.getEmail(),
                    citizen.getPassword(),
                    citizen.isActive(),
                    true,
                    true,
                    true,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CITIZEN"))
            );
        }

        throw new UsernameNotFoundException("User not found with identifier: " + username);
    }
}
