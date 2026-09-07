package com.civicpulse.config;

import com.civicpulse.model.Citizen;
import com.civicpulse.model.Complaint;
import com.civicpulse.model.Notification;
import com.civicpulse.model.Officer;
import com.civicpulse.model.TimelineEvent;
import com.civicpulse.repository.CitizenRepository;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.repository.NotificationRepository;
import com.civicpulse.repository.OfficerRepository;
import com.civicpulse.service.ComplaintService;
import com.civicpulse.service.DepartmentRoutingService;
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
            CitizenRepository citizenRepository,
            ComplaintRepository complaintRepository,
            NotificationRepository notificationRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // 1. Create Default Officers for each Department
            Officer roadsOfficer = officerRepository.findByUsername("officer1").orElse(null);
            if (roadsOfficer == null && !officerRepository.existsByEmployeeId("EMP001")) {
                roadsOfficer = new Officer();
                roadsOfficer.setUsername("officer1");
                roadsOfficer.setPassword(passwordEncoder.encode("password123"));
                roadsOfficer.setFullName("Officer One");
                roadsOfficer.setEmployeeId("EMP001");
                roadsOfficer.setEmail("officer1@civicpulse.com");
                roadsOfficer.setPhone("+91 9876543210");
                roadsOfficer.setDesignation("Senior Field Officer");
                roadsOfficer.setDepartment(DepartmentRoutingService.DEPT_ROADS);
                roadsOfficer.setRole("OFFICER");
                roadsOfficer.setJoinedDate(LocalDate.now().minusMonths(6));
                roadsOfficer.setActive(true);
                roadsOfficer = officerRepository.save(roadsOfficer);
                System.out.println(">>> Seeded default officer: officer1 (Roads / Public Works Department)");
            }

            Officer waterOfficer = officerRepository.findByUsername("officer_water").orElse(null);
            if (waterOfficer == null && !officerRepository.existsByEmployeeId("EMP002")) {
                waterOfficer = new Officer();
                waterOfficer.setUsername("officer_water");
                waterOfficer.setPassword(passwordEncoder.encode("password123"));
                waterOfficer.setFullName("Rajesh Verma");
                waterOfficer.setEmployeeId("EMP002");
                waterOfficer.setEmail("water@civicpulse.com");
                waterOfficer.setPhone("+91 9876543211");
                waterOfficer.setDesignation("Water Works Inspector");
                waterOfficer.setDepartment(DepartmentRoutingService.DEPT_WATER);
                waterOfficer.setRole("OFFICER");
                waterOfficer.setJoinedDate(LocalDate.now().minusMonths(4));
                waterOfficer.setActive(true);
                waterOfficer = officerRepository.save(waterOfficer);
                System.out.println(">>> Seeded water officer: officer_water (Water Department)");
            }

            Officer sanitationOfficer = officerRepository.findByUsername("officer_sanitation").orElse(null);
            if (sanitationOfficer == null && !officerRepository.existsByEmployeeId("EMP003")) {
                sanitationOfficer = new Officer();
                sanitationOfficer.setUsername("officer_sanitation");
                sanitationOfficer.setPassword(passwordEncoder.encode("password123"));
                sanitationOfficer.setFullName("Suresh Kumar");
                sanitationOfficer.setEmployeeId("EMP003");
                sanitationOfficer.setEmail("sanitation@civicpulse.com");
                sanitationOfficer.setPhone("+91 9876543212");
                sanitationOfficer.setDesignation("Sanitation Inspector");
                sanitationOfficer.setDepartment(DepartmentRoutingService.DEPT_SANITATION);
                sanitationOfficer.setRole("OFFICER");
                sanitationOfficer.setJoinedDate(LocalDate.now().minusMonths(3));
                sanitationOfficer.setActive(true);
                sanitationOfficer = officerRepository.save(sanitationOfficer);
                System.out.println(">>> Seeded sanitation officer: officer_sanitation (Sanitation Department)");
            }

            // 2. Create Default Citizen
            Citizen citizen = citizenRepository.findByEmail("citizen@civicpulse.com").orElse(null);
            if (citizen == null) {
                citizen = new Citizen();
                citizen.setCitizenId("CIT10045");
                citizen.setFullName("Tarun B");
                citizen.setEmail("citizen@civicpulse.com");
                citizen.setPhone("+91 9876543210");
                citizen.setPassword(passwordEncoder.encode("password123"));
                citizen.setAddress("Pragati Nagar, Sector 4, Andhra Pradesh");
                citizen.setCreatedAt(LocalDateTime.now().minusMonths(1));
                citizen.setActive(true);
                citizen = citizenRepository.save(citizen);
                System.out.println(">>> Seeded default citizen: citizen@civicpulse.com (password: password123)");
            }

            // 3. Pre-populate Demo Complaints with Dynamic Relative Dates
            LocalDateTime now = LocalDateTime.now();

            if (complaintRepository.findByComplaintNumber("CMP202600124").isEmpty()) {
                LocalDateTime c1Created = now.minusDays(2);
                Complaint c1 = new Complaint(
                        "CMP202600124",
                        "Damaged road near college",
                        "The main road leading to the college has huge potholes which are dangerous for students and motorists.",
                        "Road Damage",
                        "Assigned to Officer",
                        c1Created.format(ComplaintService.DATE_FORMATTER)
                );
                c1.setCitizenName(citizen.getFullName());
                c1.setCitizenEmail(citizen.getEmail());
                c1.setCitizenId(citizen.getCitizenId());
                c1.setLocation("College Main Road, Sector 4");
                c1.setPriority("High");
                c1.setDepartment(DepartmentRoutingService.DEPT_ROADS);
                c1.setAssignedOfficer(roadsOfficer);
                c1.setCreatedAt(c1Created);
                c1.setUpdatedAt(now.minusHours(4));
                c1.setAssignedAt(c1Created.plusHours(2));
                c1.getTimeline().add(new TimelineEvent("Complaint Submitted", c1Created.format(ComplaintService.DATE_TIME_FORMATTER), "Your complaint has been successfully submitted.", "completed"));
                c1.getTimeline().add(new TimelineEvent("Assigned to Department", c1Created.plusMinutes(30).format(ComplaintService.DATE_TIME_FORMATTER), "Routed to Roads / Public Works Department.", "completed"));
                c1.getTimeline().add(new TimelineEvent("Officer Assigned", c1Created.plusHours(2).format(ComplaintService.DATE_TIME_FORMATTER), "Your complaint has been assigned to Officer One.", "completed"));
                c1.getTimeline().add(new TimelineEvent("Under Investigation", "Pending", "Officer will inspect site and initiate road repair.", "active"));
                c1.getTimeline().add(new TimelineEvent("Resolution Pending", "Pending", "The complaint will be marked resolved after the issue is addressed.", "pending"));
                c1.getTimeline().add(new TimelineEvent("Resolved", "Pending", "Final verification and closure.", "pending"));
                complaintRepository.save(c1);
            }

            if (complaintRepository.findByComplaintNumber("CMP202600103").isEmpty()) {
                LocalDateTime c2Created = now.minusDays(4);
                Complaint c2 = new Complaint(
                        "CMP202600103",
                        "Street light not working",
                        "The street light in front of house 45 has been broken for two weeks.",
                        "Street Light",
                        "In Progress",
                        c2Created.format(ComplaintService.DATE_FORMATTER)
                );
                c2.setCitizenName(citizen.getFullName());
                c2.setCitizenEmail(citizen.getEmail());
                c2.setCitizenId(citizen.getCitizenId());
                c2.setLocation("House 45, Cross Road 2");
                c2.setPriority("Medium");
                c2.setDepartment(DepartmentRoutingService.DEPT_ELECTRICITY);
                c2.setAssignedOfficer(roadsOfficer);
                c2.setCreatedAt(c2Created);
                c2.setUpdatedAt(now.minusDays(1));
                c2.setInProgressAt(now.minusDays(1));
                c2.getTimeline().add(new TimelineEvent("Complaint Submitted", c2Created.format(ComplaintService.DATE_TIME_FORMATTER), "Your complaint has been successfully submitted.", "completed"));
                c2.getTimeline().add(new TimelineEvent("Assigned to Department", c2Created.plusMinutes(45).format(ComplaintService.DATE_TIME_FORMATTER), "Routed to Electricity Department.", "completed"));
                c2.getTimeline().add(new TimelineEvent("In Progress", now.minusDays(1).format(ComplaintService.DATE_TIME_FORMATTER), "Repair team has been dispatched to fix the street light.", "active"));
                c2.getTimeline().add(new TimelineEvent("Resolution Pending", "Pending", "Awaiting replacement bulb fitting.", "pending"));
                c2.getTimeline().add(new TimelineEvent("Resolved", "Pending", "Final inspection upon repair completion.", "pending"));
                complaintRepository.save(c2);
            }

            if (complaintRepository.findByComplaintNumber("CMP202600078").isEmpty()) {
                LocalDateTime c3Created = now.minusDays(7);
                LocalDateTime c3Resolved = now.minusDays(5);
                Complaint c3 = new Complaint(
                        "CMP202600078",
                        "Garbage collection issue",
                        "Garbage collector has not visited the street for three days.",
                        "Waste Management",
                        "Resolved",
                        c3Created.format(ComplaintService.DATE_FORMATTER)
                );
                c3.setCitizenName(citizen.getFullName());
                c3.setCitizenEmail(citizen.getEmail());
                c3.setCitizenId(citizen.getCitizenId());
                c3.setLocation("Market Square Lane 5");
                c3.setPriority("Low");
                c3.setDepartment(DepartmentRoutingService.DEPT_SANITATION);
                c3.setAssignedOfficer(sanitationOfficer);
                c3.setCreatedAt(c3Created);
                c3.setUpdatedAt(c3Resolved);
                c3.setResolvedAt(c3Resolved);
                c3.setResolution("Sanitation crew dispatched and entire market square sanitized.");
                c3.getTimeline().add(new TimelineEvent("Complaint Submitted", c3Created.format(ComplaintService.DATE_TIME_FORMATTER), "Your complaint has been successfully submitted.", "completed"));
                c3.getTimeline().add(new TimelineEvent("Assigned to Department", c3Created.plusHours(1).format(ComplaintService.DATE_TIME_FORMATTER), "Routed to Sanitation Department.", "completed"));
                c3.getTimeline().add(new TimelineEvent("Officer Assigned", c3Created.plusHours(3).format(ComplaintService.DATE_TIME_FORMATTER), "Assigned to Officer Suresh Kumar.", "completed"));
                c3.getTimeline().add(new TimelineEvent("Resolved", c3Resolved.format(ComplaintService.DATE_TIME_FORMATTER), "The garbage has been cleared and area sanitized.", "completed"));
                complaintRepository.save(c3);
            }

            // 4. Seed Dynamic Notifications for Citizen
            if (notificationRepository.countByCitizenEmailAndIsReadFalse(citizen.getEmail()) == 0) {
                notificationRepository.save(new Notification(
                        citizen.getEmail(),
                        "Complaint assigned to officer",
                        "Complaint CMP202600124 has been assigned to Officer One for inspection.",
                        "CMP202600124",
                        "ASSIGNED"
                ));
                notificationRepository.save(new Notification(
                        citizen.getEmail(),
                        "Complaint status updated",
                        "Your complaint CMP202600103 is now In Progress. Repair team dispatched.",
                        "CMP202600103",
                        "STATUS_CHANGE"
                ));
                notificationRepository.save(new Notification(
                        citizen.getEmail(),
                        "Complaint resolved",
                        "Your complaint CMP202600078 has been successfully resolved.",
                        "CMP202600078",
                        "RESOLUTION"
                ));
                notificationRepository.save(new Notification(
                        citizen.getEmail(),
                        "Welcome to CivicPulse",
                        "Welcome to the CivicPulse citizen portal. You can now report and track civic grievances in your area.",
                        null,
                        "INFO"
                ));
            }

            System.out.println(">>> Initialized dynamic demo complaints and notifications for CivicPulse.");
        };
    }
}
