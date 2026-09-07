package com.civicpulse.config;

import com.civicpulse.entites.Officer;
import com.civicpulse.entites.Admin;
import com.civicpulse.entites.Citizen;
import com.civicpulse.repositories.CitizenRepository;
import com.civicpulse.repositories.AdminRepository;
import com.civicpulse.repositories.OfficerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createDefaultOfficer(
            OfficerRepository officerRepository,
            AdminRepository adminRepository,
            CitizenRepository citizenRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // Default Admin
            if (adminRepository.findByUsername("admin").isEmpty()) {
                Admin admin = new Admin(
                        "CivicPulse Administrator",
                        "admin",
                        passwordEncoder.encode("admin123"),
                        "admin@civicpulse.com",
                        "9999999999"
                );

                adminRepository.save(admin);
                System.out.println("Default admin created: admin / admin123");
            }

            // Default Officer
            if (officerRepository.findByUsername("officer1").isEmpty()
                    && !officerRepository.existsByEmployeeId("EMP001")) {

                Officer officer = new Officer();

                officer.setUsername("officer1");
                officer.setPassword(passwordEncoder.encode("password123"));
                officer.setFullName("Officer One");
                officer.setEmployeeId("EMP001");
                officer.setEmail("officer1@civicpulse.com");
                officer.setDesignation("Electricity Officer");
                officer.setDepartment("Electricity");
                officer.setRole("OFFICER");
                officer.setJoinedDate(LocalDate.now());
                officer.setActive(true);

                officerRepository.save(officer);

                System.out.println("Default officer created: officer1");
            }

            // Default Citizen
            if (citizenRepository.findByUsername("citizen1").isEmpty()
                    && !citizenRepository.existsByCitizenId("CIT001")) {

                Citizen citizen = new Citizen();

                citizen.setUsername("citizen1");
                citizen.setPassword(passwordEncoder.encode("citizen123"));
                citizen.setFullName("Citizen One");
                citizen.setCitizenId("CIT001");
                citizen.setEmail("citizen1@civicpulse.com");
                citizen.setPhone("9876543210");
                citizen.setAddress("Sector 62, Noida");
                citizen.setRole("CITIZEN");
                citizen.setActive(true);

                citizenRepository.save(citizen);

                System.out.println("Default citizen created: citizen1 / citizen123");
            }
        };
    }
}