package com.civicpulse.security;

import com.civicpulse.model.Citizen;
import com.civicpulse.model.Officer;
import com.civicpulse.repository.CitizenRepository;
import com.civicpulse.repository.OfficerRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
public class SecurityConfig {

    private final CitizenRepository citizenRepository;
    private final OfficerRepository officerRepository;

    public SecurityConfig(
            @Lazy CitizenRepository citizenRepository,
            OfficerRepository officerRepository) {

        this.citizenRepository = citizenRepository;
        this.officerRepository = officerRepository;
    }

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================================================
    // DEFAULT ADMIN ACCOUNT
    // =========================================================

    @Bean
    public CommandLineRunner createDefaultAdmin(
            PasswordEncoder passwordEncoder) {

        return args -> {

            Officer admin = officerRepository
                    .findByUsername("admin")
                    .orElseGet(Officer::new);

            admin.setUsername("admin");

            /*
             * Always set BCrypt password.
             * Login password = admin123
             */
            admin.setPassword(
                    passwordEncoder.encode("admin123")
            );

            admin.setFullName("Administrator");
            admin.setEmployeeId("ADM001");
            admin.setEmail("admin@civicpulse.com");
            admin.setPhone("");
            admin.setDesignation("System Administrator");
            admin.setDepartment("Administration");
            admin.setAssignedArea("All Areas");
            admin.setOfficeLocation("CivicPulse Administration");
            admin.setAddress("CivicPulse Administrative Office");
            admin.setRole("ADMIN");
            admin.setJurisdiction("All");
            admin.setActive(true);

            officerRepository.save(admin);

            System.out.println("==============================================");
            System.out.println("        CIVICPULSE ADMIN ACCOUNT READY");
            System.out.println("==============================================");
            System.out.println("Username : admin");
            System.out.println("Password : admin123");
            System.out.println("Role     : ADMIN");
            System.out.println("Status   : ACTIVE");
            System.out.println("==============================================");
        };
    }

    // =========================================================
    // LOGIN SUCCESS HANDLER
    // =========================================================

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {

        return (
                HttpServletRequest request,
                HttpServletResponse response,
                Authentication authentication
        ) -> {

            boolean isCitizen =
                    authentication.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(role ->
                                    role.equals("ROLE_CITIZEN")
                            );

            boolean isAdmin =
                    authentication.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(role ->
                                    role.equals("ROLE_ADMIN")
                            );

            // =================================================
            // CITIZEN
            // =================================================

            if (isCitizen) {

                String principalName =
                        authentication.getName();

                Optional<Citizen> citizenOpt =
                        citizenRepository
                                .findByEmailIgnoreCase(
                                        principalName
                                );

                if (citizenOpt.isEmpty()) {

                    citizenOpt =
                            citizenRepository
                                    .findByCitizenIdIgnoreCase(
                                            principalName
                                    );
                }

                if (citizenOpt.isEmpty()) {

                    citizenOpt =
                            citizenRepository
                                    .findByUsernameIgnoreCase(
                                            principalName
                                    );
                }

                if (citizenOpt.isPresent()) {

                    Citizen citizen =
                            citizenOpt.get();

                    citizen.setLastLogin(
                            LocalDateTime.now()
                    );

                    citizenRepository.save(citizen);

                    HttpSession session =
                            request.getSession(true);

                    session.setAttribute(
                            "CITIZEN_EMAIL",
                            citizen.getEmail()
                    );

                    session.setAttribute(
                            "CITIZEN_NAME",
                            citizen.getFullName()
                    );

                    session.setAttribute(
                            "CITIZEN_ID",
                            citizen.getCitizenId()
                    );
                }

                response.sendRedirect(
                        "/citizen/dashboard"
                );

            }

            // =================================================
            // ADMIN
            // =================================================

            else if (isAdmin) {

                response.sendRedirect(
                        "/admin/dashboard"
                );

            }

            // =================================================
            // OFFICER
            // =================================================

            else {

                response.sendRedirect(
                        "/officer/dashboard"
                );
            }
        };
    }

    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                // Current project uses AJAX/API POST requests
                .csrf(csrf -> csrf.disable())

                // Allow H2/other embedded frames if needed
                .headers(headers ->
                        headers.frameOptions(frameOptions ->
                                frameOptions.disable()
                        )
                )

                // =================================================
                // AUTHORIZATION
                // =================================================

                .authorizeHttpRequests(auth -> auth

                        // -----------------------------------------
                        // PUBLIC
                        // -----------------------------------------

                        .requestMatchers(
                                "/",
                                "/officer/login",
                                "/citizen/login",
                                "/citizen/signup",
                                "/citizen/register",
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**",
                                "/favicon.ico"
                        ).permitAll()

                        // -----------------------------------------
                        // ADMIN ONLY
                        // -----------------------------------------

                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // -----------------------------------------
                        // OFFICER ONLY
                        // -----------------------------------------

                        .requestMatchers("/officer/**")
                        .hasRole("OFFICER")

                        // -----------------------------------------
                        // CITIZEN ONLY
                        // -----------------------------------------

                        .requestMatchers("/citizen/**")
                        .hasRole("CITIZEN")

                        // -----------------------------------------
                        // EVERYTHING ELSE
                        // -----------------------------------------

                        .anyRequest()
                        .authenticated()
                )

                // =================================================
                // FORM LOGIN
                // =================================================

                .formLogin(form -> form

                        .loginPage("/officer/login")

                        .loginProcessingUrl("/login")

                        .successHandler(
                                customAuthenticationSuccessHandler()
                        )

                        .permitAll()
                )

                // =================================================
                // LOGOUT
                // =================================================

                .logout(logout -> logout

                        .logoutUrl("/logout")

                        .logoutSuccessUrl(
                                "/officer/login?logout"
                        )

                        .invalidateHttpSession(true)

                        .clearAuthentication(true)

                        .permitAll()
                );

        return http.build();
    }
}