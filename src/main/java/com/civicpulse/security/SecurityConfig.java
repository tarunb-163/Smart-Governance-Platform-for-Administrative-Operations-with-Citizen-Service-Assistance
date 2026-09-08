package com.civicpulse.security;

import com.civicpulse.model.Citizen;
import com.civicpulse.repository.CitizenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

    public SecurityConfig(@Lazy CitizenRepository citizenRepository) {
        this.citizenRepository = citizenRepository;
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
                }

                response.sendRedirect("/citizen/dashboard");
            } else if (isAdmin) {
                response.sendRedirect("/admin/dashboard");
            } else {
                response.sendRedirect("/officer/dashboard");
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
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/officer/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                );

        return http.build();
    }
}