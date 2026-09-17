package com.civicpulse.config;

import com.civicpulse.model.Citizen;
import com.civicpulse.model.Complaint;
import com.civicpulse.model.ComplaintAttachment;
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
                roadsOfficer.setAssignedArea("Central Ward & Sector 4");
                roadsOfficer.setOfficeLocation("Municipal PWD Headquarters, Zone 1");
                roadsOfficer.setAddress("Room 204, PWD Administrative Block, Civic Center, Andhra Pradesh");
                roadsOfficer.setJurisdiction("Municipal North & Central Zones");
                roadsOfficer.setRole("OFFICER");
                roadsOfficer.setJoinedDate(LocalDate.now().minusMonths(6));
                roadsOfficer.setActive(true);
                roadsOfficer = officerRepository.save(roadsOfficer);
                System.out.println(">>> Seeded default officer: officer1 (Roads / Public Works Department)");
            } else if (roadsOfficer != null) {
                boolean updated = false;
                if (roadsOfficer.getAssignedArea() == null || roadsOfficer.getAssignedArea().trim().isEmpty()) {
                    roadsOfficer.setAssignedArea("Central Ward & Sector 4");
                    updated = true;
                }
                if (roadsOfficer.getOfficeLocation() == null || roadsOfficer.getOfficeLocation().trim().isEmpty()) {
                    roadsOfficer.setOfficeLocation("Municipal PWD Headquarters, Zone 1");
                    updated = true;
                }
                if (roadsOfficer.getAddress() == null || roadsOfficer.getAddress().trim().isEmpty()) {
                    roadsOfficer.setAddress("Room 204, PWD Administrative Block, Civic Center, Andhra Pradesh");
                    updated = true;
                }
                if (roadsOfficer.getJurisdiction() == null || roadsOfficer.getJurisdiction().trim().isEmpty()) {
                    roadsOfficer.setJurisdiction("Municipal North & Central Zones");
                    updated = true;
                }
                if (updated) {
                    roadsOfficer = officerRepository.save(roadsOfficer);
                }
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
                waterOfficer.setAssignedArea("Ward 12 & Water Distribution Zone B");
                waterOfficer.setOfficeLocation("Water Works Division Office, Sector 2");
                waterOfficer.setAddress("Water Supply Board, Main Treatment Plant, Andhra Pradesh");
                waterOfficer.setJurisdiction("Zone 2 Water Supply & Drainage");
                waterOfficer.setRole("OFFICER");
                waterOfficer.setJoinedDate(LocalDate.now().minusMonths(4));
                waterOfficer.setActive(true);
                waterOfficer = officerRepository.save(waterOfficer);
                System.out.println(">>> Seeded water officer: officer_water (Water Department)");
            } else if (waterOfficer != null) {
                boolean updated = false;
                if (waterOfficer.getAssignedArea() == null || waterOfficer.getAssignedArea().trim().isEmpty()) {
                    waterOfficer.setAssignedArea("Ward 12 & Water Distribution Zone B");
                    updated = true;
                }
                if (waterOfficer.getOfficeLocation() == null || waterOfficer.getOfficeLocation().trim().isEmpty()) {
                    waterOfficer.setOfficeLocation("Water Works Division Office, Sector 2");
                    updated = true;
                }
                if (waterOfficer.getAddress() == null || waterOfficer.getAddress().trim().isEmpty()) {
                    waterOfficer.setAddress("Water Supply Board, Main Treatment Plant, Andhra Pradesh");
                    updated = true;
                }
                if (waterOfficer.getJurisdiction() == null || waterOfficer.getJurisdiction().trim().isEmpty()) {
                    waterOfficer.setJurisdiction("Zone 2 Water Supply & Drainage");
                    updated = true;
                }
                if (updated) {
                    waterOfficer = officerRepository.save(waterOfficer);
                }
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
                sanitationOfficer.setAssignedArea("Ward 05 & Market Square Area");
                sanitationOfficer.setOfficeLocation("Sanitation & Waste Management Depo, Zone 3");
                sanitationOfficer.setAddress("Civic Health & Sanitation Complex, Andhra Pradesh");
                sanitationOfficer.setJurisdiction("Market District & Commercial Zones");
                sanitationOfficer.setRole("OFFICER");
                sanitationOfficer.setJoinedDate(LocalDate.now().minusMonths(3));
                sanitationOfficer.setActive(true);
                sanitationOfficer = officerRepository.save(sanitationOfficer);
                System.out.println(">>> Seeded sanitation officer: officer_sanitation (Sanitation Department)");
            } else if (sanitationOfficer != null) {
                boolean updated = false;
                if (sanitationOfficer.getAssignedArea() == null || sanitationOfficer.getAssignedArea().trim().isEmpty()) {
                    sanitationOfficer.setAssignedArea("Ward 05 & Market Square Area");
                    updated = true;
                }
                if (sanitationOfficer.getOfficeLocation() == null || sanitationOfficer.getOfficeLocation().trim().isEmpty()) {
                    sanitationOfficer.setOfficeLocation("Sanitation & Waste Management Depo, Zone 3");
                    updated = true;
                }
                if (sanitationOfficer.getAddress() == null || sanitationOfficer.getAddress().trim().isEmpty()) {
                    sanitationOfficer.setAddress("Civic Health & Sanitation Complex, Andhra Pradesh");
                    updated = true;
                }
                if (sanitationOfficer.getJurisdiction() == null || sanitationOfficer.getJurisdiction().trim().isEmpty()) {
                    sanitationOfficer.setJurisdiction("Market District & Commercial Zones");
                    updated = true;
                }
                if (updated) {
                    sanitationOfficer = officerRepository.save(sanitationOfficer);
                }
            }

            Officer electricityOfficer = officerRepository.findByUsername("officer_electricity").orElse(null);
            if (electricityOfficer == null && !officerRepository.existsByEmployeeId("EMP004")) {
                electricityOfficer = new Officer();
                electricityOfficer.setUsername("officer_electricity");
                electricityOfficer.setPassword(passwordEncoder.encode("password123"));
                electricityOfficer.setFullName("K. Ramesh Babu");
                electricityOfficer.setEmployeeId("EMP004");
                electricityOfficer.setEmail("electricity@civicpulse.com");
                electricityOfficer.setPhone("+91 9876543213");
                electricityOfficer.setDesignation("Power Grid & Electrical Supervisor");
                electricityOfficer.setDepartment(DepartmentRoutingService.DEPT_ELECTRICITY);
                electricityOfficer.setAssignedArea("Ward 08 & Substation Sector 5");
                electricityOfficer.setOfficeLocation("City Power Transmission Station, North Wing");
                electricityOfficer.setAddress("Electricity Board Operations Office, Andhra Pradesh");
                electricityOfficer.setJurisdiction("North Grid & Street Lighting Zone");
                electricityOfficer.setRole("OFFICER");
                electricityOfficer.setJoinedDate(LocalDate.now().minusMonths(2));
                electricityOfficer.setActive(true);
                electricityOfficer = officerRepository.save(electricityOfficer);
                System.out.println(">>> Seeded electricity officer: officer_electricity (Electricity Department)");
            } else if (electricityOfficer != null) {
                boolean updated = false;
                if (electricityOfficer.getAssignedArea() == null || electricityOfficer.getAssignedArea().trim().isEmpty()) {
                    electricityOfficer.setAssignedArea("Ward 08 & Substation Sector 5");
                    updated = true;
                }
                if (electricityOfficer.getOfficeLocation() == null || electricityOfficer.getOfficeLocation().trim().isEmpty()) {
                    electricityOfficer.setOfficeLocation("City Power Transmission Station, North Wing");
                    updated = true;
                }
                if (electricityOfficer.getAddress() == null || electricityOfficer.getAddress().trim().isEmpty()) {
                    electricityOfficer.setAddress("Electricity Board Operations Office, Andhra Pradesh");
                    updated = true;
                }
                if (electricityOfficer.getJurisdiction() == null || electricityOfficer.getJurisdiction().trim().isEmpty()) {
                    electricityOfficer.setJurisdiction("North Grid & Street Lighting Zone");
                    updated = true;
                }
                if (updated) {
                    electricityOfficer = officerRepository.save(electricityOfficer);
                }
            }

            // 1B. Create Default Administrator
            Officer adminUser = officerRepository.findByUsername("admin").orElse(null);
            if (adminUser == null && !officerRepository.existsByEmployeeId("ADM001")) {
                adminUser = new Officer();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setFullName("Dr. S. K. Ramanathan");
                adminUser.setEmployeeId("ADM001");
                adminUser.setEmail("admin@civicpulse.com");
                adminUser.setPhone("+91 9876543200");
                adminUser.setDesignation("Chief Municipal Administrator");
                adminUser.setDepartment(DepartmentRoutingService.DEPT_GENERAL);
                adminUser.setAssignedArea("All Municipal Zones (City-wide)");
                adminUser.setOfficeLocation("Municipal Corporation HQ, Executive Suite 101");
                adminUser.setAddress("City Hall, Municipal Secretariat Complex, Andhra Pradesh");
                adminUser.setJurisdiction("Metropolitan Municipal Region");
                adminUser.setRole("ADMIN");
                adminUser.setJoinedDate(LocalDate.now().minusYears(2));
                adminUser.setActive(true);
                adminUser = officerRepository.save(adminUser);
                System.out.println(">>> Seeded default administrator: admin / admin123 (ROLE_ADMIN)");
            } else if (adminUser != null) {
                boolean updated = false;
                if (adminUser.getAssignedArea() == null || adminUser.getAssignedArea().trim().isEmpty()) {
                    adminUser.setAssignedArea("All Municipal Zones (City-wide)");
                    updated = true;
                }
                if (adminUser.getOfficeLocation() == null || adminUser.getOfficeLocation().trim().isEmpty()) {
                    adminUser.setOfficeLocation("Municipal Corporation HQ, Executive Suite 101");
                    updated = true;
                }
                if (adminUser.getAddress() == null || adminUser.getAddress().trim().isEmpty()) {
                    adminUser.setAddress("City Hall, Municipal Secretariat Complex, Andhra Pradesh");
                    updated = true;
                }
                if (adminUser.getJurisdiction() == null || adminUser.getJurisdiction().trim().isEmpty()) {
                    adminUser.setJurisdiction("Metropolitan Municipal Region");
                    updated = true;
                }
                if (updated) {
                    adminUser = officerRepository.save(adminUser);
                }
            }

            // 2. Create Default Citizen
            Citizen citizen = citizenRepository.findByEmailIgnoreCase("citizen@civicpulse.com").orElse(null);
            if (citizen == null) {
                citizen = new Citizen();
                citizen.setCitizenId("CIT10045");
                citizen.setFullName("Tarun B");
                citizen.setUsername("tarun");
                citizen.setEmail("citizen@civicpulse.com");
                citizen.setPhone("+91 9876543210");
                citizen.setPassword(passwordEncoder.encode("password123"));
                citizen.setAddress("Pragati Nagar, Sector 4, Andhra Pradesh");
                citizen.setCreatedAt(LocalDateTime.now().minusMonths(1));
                citizen.setLastLogin(LocalDateTime.now().minusHours(2));
                citizen.setActive(true);
                citizen = citizenRepository.save(citizen);
                System.out.println(">>> Seeded default citizen: citizen@civicpulse.com / tarun (password: password123)");
            }

            // 3. Pre-populate Demo Complaints with Dynamic Relative Dates
            LocalDateTime now = LocalDateTime.now();

            Complaint c1 = complaintRepository.findByComplaintNumber("CMP202600124").orElse(null);
            if (c1 == null) {
                LocalDateTime c1Created = now.minusDays(2);
                c1 = new Complaint(
                        "CMP202600124",
                        "Damaged road near college",
                        "The main road leading to the college has huge potholes which are dangerous for students and motorists.",
                        "Road Damage",
                        "Assigned to Officer",
                        c1Created.format(ComplaintService.DATE_FORMATTER)
                );
                c1.setCitizenName(citizen.getFullName());
                c1.setCitizenEmail(citizen.getEmail());
                c1.setCitizenContact(citizen.getPhone());
                c1.setCitizenId(citizen.getCitizenId());
                c1.setLocation("College Main Road, Sector 4");
                c1.setPriority("High");
                c1.setDepartment(DepartmentRoutingService.DEPT_ROADS);
                c1.setAssignedOfficer(roadsOfficer);
                c1.setCreatedAt(c1Created);
                c1.setUpdatedAt(now.minusHours(4));
                c1.setAssignedAt(c1Created.plusHours(2));
                c1.setImagePath("uploads/CMP202600004_1789642240674.jpg");
                ComplaintAttachment att = new ComplaintAttachment(
                        c1,
                        "road_pothole_evidence.jpg",
                        "CMP202600004_1789642240674.jpg",
                        "image/jpeg",
                        63285L,
                        "uploads/CMP202600004_1789642240674.jpg"
                );
                c1.addAttachment(att);
                c1.getTimeline().add(new TimelineEvent("Complaint Submitted", c1Created.format(ComplaintService.DATE_TIME_FORMATTER), "Your complaint has been successfully submitted.", "completed"));
                c1.getTimeline().add(new TimelineEvent("Assigned to Department", c1Created.plusMinutes(30).format(ComplaintService.DATE_TIME_FORMATTER), "Routed to Roads / Public Works Department.", "completed"));
                c1.getTimeline().add(new TimelineEvent("Officer Assigned", c1Created.plusHours(2).format(ComplaintService.DATE_TIME_FORMATTER), "Your complaint has been assigned to Officer One.", "completed"));
                c1.getTimeline().add(new TimelineEvent("Under Investigation", "Pending", "Officer will inspect site and initiate road repair.", "active"));
                c1.getTimeline().add(new TimelineEvent("Resolution Pending", "Pending", "The complaint will be marked resolved after the issue is addressed.", "pending"));
                c1.getTimeline().add(new TimelineEvent("Resolved", "Pending", "Final verification and closure.", "pending"));
                complaintRepository.save(c1);
            } else if (c1.getImagePath() == null) {
                c1.setImagePath("uploads/CMP202600004_1789642240674.jpg");
                ComplaintAttachment att = new ComplaintAttachment(
                        c1,
                        "road_pothole_evidence.jpg",
                        "CMP202600004_1789642240674.jpg",
                        "image/jpeg",
                        63285L,
                        "uploads/CMP202600004_1789642240674.jpg"
                );
                c1.addAttachment(att);
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
                c2.setCitizenContact(citizen.getPhone());
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
                c3.setCitizenContact(citizen.getPhone());
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

            // 5. Seed Dynamic Notifications for Officer
            if (notificationRepository.countByOfficerUsernameAndIsReadFalse("officer1") == 0) {
                notificationRepository.save(Notification.forOfficer(
                        "officer1",
                        "New Grievance Assigned",
                        "Grievance CMP202600124 (Damaged road near college) has been assigned to your department.",
                        "CMP202600124",
                        "ASSIGNED"
                ));
                notificationRepository.save(Notification.forOfficer(
                        "officer1",
                        "High Priority Inspection Required",
                        "Road Damage complaint CMP202600125 flagged as High Priority. Immediate field inspection required.",
                        "CMP202600125",
                        "STATUS_CHANGE"
                ));
                notificationRepository.save(Notification.forOfficer(
                        "officer1",
                        "Citizen Feedback Received",
                        "Citizen submitted 5-star positive feedback on resolved complaint CMP202600078.",
                        "CMP202600078",
                        "INFO"
                ));
            }

            System.out.println(">>> Initialized dynamic demo complaints and notifications for CivicPulse.");
        };
    }
}
