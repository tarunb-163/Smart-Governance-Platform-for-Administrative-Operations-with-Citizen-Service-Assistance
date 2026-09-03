package com.civicpulse.config;

import com.civicpulse.model.Complaint;
import com.civicpulse.model.Officer;
import com.civicpulse.model.TimelineEvent;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.repository.OfficerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            OfficerRepository officerRepository,
            ComplaintRepository complaintRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // 1. Create Default Officer if missing
            Officer officer = officerRepository.findByUsername("officer1").orElse(null);
            if (officer == null && !officerRepository.existsByEmployeeId("EMP001")) {
                officer = new Officer();
                officer.setUsername("officer1");
                officer.setPassword(passwordEncoder.encode("password123"));
                officer.setFullName("Officer One");
                officer.setEmployeeId("EMP001");
                officer.setEmail("officer1@civicpulse.com");
                officer.setPhone("+91 9876543210");
                officer.setDesignation("Senior Field Officer");
                officer.setDepartment("Electricity & Public Works");
                officer.setRole("OFFICER");
                officer.setJoinedDate(LocalDate.now());
                officer.setActive(true);

                officer = officerRepository.save(officer);
                System.out.println(">>> Seeded default officer: officer1 (password: password123)");
            }

            // 2. Pre-populate Demo Complaints if missing
            if (complaintRepository.findByComplaintNumber("CMP202600124").isEmpty()) {
                Complaint c1 = new Complaint(
                        "CMP202600124",
                        "Damaged road near college",
                        "The main road leading to the college has huge potholes which are dangerous for students and motorists.",
                        "Road Damage",
                        "Assigned to Officer",
                        "11 Aug 2026"
                );
                c1.setCitizenName("Tarun B");
                c1.setLocation("College Main Road, Sector 4");
                c1.setPriority("High");
                c1.setDepartment("Public Works");
                c1.setAssignedOfficer(officer);
                c1.setAssignedAt(LocalDateTime.now().minusDays(2));
                c1.getTimeline().add(new TimelineEvent("Complaint Submitted", "11 Aug 2026, 10:30 AM", "Your complaint has been successfully submitted.", "completed"));
                c1.getTimeline().add(new TimelineEvent("Under Review", "11 Aug 2026, 11:15 AM", "The complaint has been reviewed by the concerned department.", "completed"));
                c1.getTimeline().add(new TimelineEvent("Assigned to Officer", "11 Aug 2026, 12:00 PM", "Your complaint has been assigned to a concerned officer.", "active"));
                c1.getTimeline().add(new TimelineEvent("Resolution", "Pending", "The complaint will be marked resolved after the issue is addressed.", "pending"));
                complaintRepository.save(c1);
            }

            if (complaintRepository.findByComplaintNumber("CMP202600103").isEmpty()) {
                Complaint c2 = new Complaint(
                        "CMP202600103",
                        "Street light not working",
                        "The street light in front of house 45 has been broken for two weeks.",
                        "Street Light",
                        "In Progress",
                        "08 Aug 2026"
                );
                c2.setCitizenName("Ayesha Khan");
                c2.setLocation("House 45, Cross Road 2");
                c2.setPriority("Medium");
                c2.setDepartment("Electricity");
                c2.setAssignedOfficer(officer);
                c2.setInProgressAt(LocalDateTime.now().minusDays(1));
                c2.getTimeline().add(new TimelineEvent("Complaint Submitted", "08 Aug 2026, 09:00 AM", "Your complaint has been successfully submitted.", "completed"));
                c2.getTimeline().add(new TimelineEvent("Under Review", "08 Aug 2026, 02:00 PM", "The complaint has been reviewed by the concerned department.", "completed"));
                c2.getTimeline().add(new TimelineEvent("In Progress", "09 Aug 2026, 10:00 AM", "Repair team has been dispatched to fix the light.", "active"));
                c2.getTimeline().add(new TimelineEvent("Resolution", "Pending", "The complaint will be marked resolved after the issue is addressed.", "pending"));
                complaintRepository.save(c2);
            }

            if (complaintRepository.findByComplaintNumber("CMP202600078").isEmpty()) {
                Complaint c3 = new Complaint(
                        "CMP202600078",
                        "Garbage collection issue",
                        "Garbage collector has not visited the street for three days.",
                        "Waste Management",
                        "Resolved",
                        "01 Aug 2026"
                );
                c3.setCitizenName("Ramesh Sharma");
                c3.setLocation("Market Square Lane 5");
                c3.setPriority("Low");
                c3.setDepartment("Sanitation");
                c3.setAssignedOfficer(officer);
                c3.setResolvedAt(LocalDateTime.now().minusDays(5));
                c3.setResolution("Sanitation crew dispatched and area sanitized.");
                c3.getTimeline().add(new TimelineEvent("Complaint Submitted", "01 Aug 2026, 08:30 AM", "Your complaint has been successfully submitted.", "completed"));
                c3.getTimeline().add(new TimelineEvent("Under Review", "01 Aug 2026, 11:00 AM", "The complaint has been reviewed by the concerned department.", "completed"));
                c3.getTimeline().add(new TimelineEvent("Assigned to Officer", "01 Aug 2026, 03:00 PM", "Your complaint has been assigned to a concerned officer.", "completed"));
                c3.getTimeline().add(new TimelineEvent("Resolved", "02 Aug 2026, 04:00 PM", "The garbage has been cleared and area sanitized.", "completed"));
                complaintRepository.save(c3);
            }

            if (complaintRepository.findByComplaintNumber("CMP202600999").isEmpty()) {
                Complaint c4 = new Complaint(
                        "CMP202600999",
                        "Street Light Not Working",
                        "Street light near the main road is not functioning.",
                        "Electrical",
                        "Resolved",
                        "20 Aug 2026"
                );
                c4.setCitizenName("Priya Patel");
                c4.setLocation("North Avenue Bus Stop");
                c4.setPriority("Medium");
                c4.setDepartment("Electricity");
                c4.setAssignedOfficer(officer);
                c4.setResolvedAt(LocalDateTime.now().minusDays(10));
                c4.setResolution("Street light bulb replaced and verified operational.");
                c4.getTimeline().add(new TimelineEvent("Complaint Submitted", "20 Aug 2026, 09:00 AM", "Your complaint has been successfully submitted.", "completed"));
                c4.getTimeline().add(new TimelineEvent("Under Review", "20 Aug 2026, 02:00 PM", "The complaint has been reviewed by the concerned department.", "completed"));
                c4.getTimeline().add(new TimelineEvent("In Progress", "21 Aug 2026, 10:00 AM", "Maintenance team dispatched.", "completed"));
                c4.getTimeline().add(new TimelineEvent("Resolved", "22 Aug 2026, 04:00 PM", "Street light bulb replaced and verified operational.", "completed"));
                complaintRepository.save(c4);
            }

            System.out.println(">>> Pre-populated 4 demo complaints for Citizen, Admin, and Officer modules.");
        };
    }
}
