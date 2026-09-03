package com.civicpulse.service;

import com.civicpulse.model.Officer;
import com.civicpulse.repository.OfficerRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final OfficerRepository officerRepository;

    public CustomUserDetailsService(OfficerRepository officerRepository) {
        this.officerRepository = officerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Officer officer = officerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Officer not found with username: " + username));

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
}
