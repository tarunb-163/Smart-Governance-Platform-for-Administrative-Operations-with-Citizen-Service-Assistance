package com.civicpulse.security;

import com.civicpulse.model.Citizen;
import com.civicpulse.model.Officer;
import com.civicpulse.repository.CitizenRepository;
import com.civicpulse.repository.OfficerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
public class SecurityConfig {

    private final CitizenRepository citizenRepository;
    private final OfficerRepository officerRepository;

    public SecurityConfig(@Lazy CitizenRepository citizenRepository, @Lazy OfficerRepository officerRepository) {
        this.citizenRepository = citizenRepository;
        this.officerRepository = officerRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            boolean isCitizen = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_CITIZEN"));

            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN"));

            String loginType = request.getParameter("loginType");
            String referer = request.getHeader("Referer");

            // Safeguard: Check role matches login portal intent
            if ("admin".equalsIgnoreCase(loginType) || (referer != null && referer.contains("/admin/login"))) {
                if (!isAdmin) {
                    HttpSession session = request.getSession(false);
                    if (session != null) session.invalidate();
                    response.sendRedirect("/admin/login?error=unauthorized");
                    return;
                }
            } else if ("officer".equalsIgnoreCase(loginType) || (referer != null && referer.contains("/officer/login"))) {
                if (isCitizen) {
                    HttpSession session = request.getSession(false);
                    if (session != null) session.invalidate();
                    response.sendRedirect("/officer/login?error=unauthorized");
                    return;
                }
            }

            if (isCitizen) {
                String principalName = authentication.getName();
                Optional<Citizen> citizenOpt = citizenRepository.findByEmailIgnoreCase(principalName);
                if (citizenOpt.isEmpty()) {
                    citizenOpt = citizenRepository.findByCitizenIdIgnoreCase(principalName);
                }
                if (citizenOpt.isEmpty()) {
                    citizenOpt = citizenRepository.findByUsernameIgnoreCase(principalName);
                }

                if (citizenOpt.isPresent()) {
                    Citizen citizen = citizenOpt.get();
                    citizen.setLastLogin(LocalDateTime.now());
                    citizenRepository.save(citizen);

                    HttpSession session = request.getSession(true);
                    session.setAttribute("CITIZEN_EMAIL", citizen.getEmail());
                    session.setAttribute("CITIZEN_NAME", citizen.getFullName());
                    session.setAttribute("CITIZEN_ID", citizen.getCitizenId());
                    if (citizen.getUsername() != null) {
                        session.setAttribute("CITIZEN_USERNAME", citizen.getUsername());
                    }
                }

                response.sendRedirect("/citizen/dashboard");
            } else if (isAdmin) {
                String principalName = authentication.getName();
                Optional<Officer> adminOpt = officerRepository.findByUsername(principalName);
                HttpSession session = request.getSession(true);
                session.setAttribute("ADMIN_USERNAME", principalName);
                session.setAttribute("USER_ROLE", "ADMIN");
                if (adminOpt.isPresent()) {
                    Officer adminObj = adminOpt.get();
                    session.setAttribute("ADMIN_NAME", adminObj.getFullName());
                    session.setAttribute("ADMIN_EMAIL", adminObj.getEmail());
                    session.setAttribute("ADMIN_DESIGNATION", adminObj.getDesignation());
                } else {
                    session.setAttribute("ADMIN_NAME", "System Administrator");
                }

                response.sendRedirect("/admin/dashboard");
            } else {
                String principalName = authentication.getName();
                Optional<Officer> officerOpt = officerRepository.findByUsername(principalName);
                HttpSession session = request.getSession(true);
                session.setAttribute("OFFICER_USERNAME", principalName);
                session.setAttribute("USER_ROLE", "OFFICER");
                if (officerOpt.isPresent()) {
                    Officer off = officerOpt.get();
                    session.setAttribute("OFFICER_NAME", off.getFullName());
                    session.setAttribute("OFFICER_DEPARTMENT", off.getDepartment());
                    session.setAttribute("OFFICER_EMPLOYEE_ID", off.getEmployeeId());
                    session.setAttribute("OFFICER_DESIGNATION", off.getDesignation());
                }

                response.sendRedirect("/officer/dashboard");
            }
        };
    }

    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) -> {
            String loginType = request.getParameter("loginType");
            String referer = request.getHeader("Referer");
            String dept = request.getParameter("dept");

            if ("admin".equalsIgnoreCase(loginType) || (referer != null && referer.contains("/admin/login"))) {
                response.sendRedirect("/admin/login?error=true");
            } else if (referer != null && referer.contains("/citizen/login")) {
                response.sendRedirect("/citizen/login?error=true");
            } else {
                if (dept != null && !dept.trim().isEmpty()) {
                    response.sendRedirect("/officer/login?dept=" + dept.trim() + "&error=true");
                } else {
                    response.sendRedirect("/officer/login?error=true");
                }
            }
        };
    }

    @Bean
    public LogoutSuccessHandler customLogoutSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            String referer = request.getHeader("Referer");
            String target = request.getParameter("from");

            if ("admin".equalsIgnoreCase(target) || (referer != null && referer.contains("/admin"))) {
                response.sendRedirect("/admin/login?logout=true");
            } else if ("citizen".equalsIgnoreCase(target) || (referer != null && referer.contains("/citizen"))) {
                response.sendRedirect("/citizen/login?logout=true");
            } else {
                response.sendRedirect("/officer/login?logout=true");
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/officer/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureHandler(customAuthenticationFailureHandler())
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(customLogoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                );

        return http.build();
    }
}