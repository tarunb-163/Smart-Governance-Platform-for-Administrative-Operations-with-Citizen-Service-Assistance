package com.civicpulse.service;

import com.civicpulse.model.Complaint;
import com.civicpulse.model.TimelineEvent;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ComplaintService {

    private final Map<String, Complaint> complaints = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Pre-populate data matching complaints.html and track.html

        // 1. CMP202600124 (Assigned to Officer)
        Complaint c1 = new Complaint(
            "CMP202600124",
            "Damaged road near college",
            "The main road leading to the college has huge potholes which are dangerous for students and motorists.",
            "Road Damage",
            "Assigned to Officer",
            "11 Aug 2026"
        );
        c1.getTimeline().add(new TimelineEvent("Complaint Submitted", "11 Aug 2026, 10:30 AM", "Your complaint has been successfully submitted.", "completed"));
        c1.getTimeline().add(new TimelineEvent("Under Review", "11 Aug 2026, 11:15 AM", "The complaint has been reviewed by the concerned department.", "completed"));
        c1.getTimeline().add(new TimelineEvent("Assigned to Officer", "11 Aug 2026, 12:00 PM", "Your complaint has been assigned to a concerned officer.", "active"));
        c1.getTimeline().add(new TimelineEvent("Resolution", "Pending", "The complaint will be marked resolved after the issue is addressed.", "pending"));
        complaints.put(c1.getId(), c1);

        // 2. CMP202600103 (In Progress)
        Complaint c2 = new Complaint(
            "CMP202600103",
            "Street light not working",
            "The street light in front of house 45 has been broken for two weeks.",
            "Street Light",
            "In Progress",
            "08 Aug 2026"
        );
        c2.getTimeline().add(new TimelineEvent("Complaint Submitted", "08 Aug 2026, 09:00 AM", "Your complaint has been successfully submitted.", "completed"));
        c2.getTimeline().add(new TimelineEvent("Under Review", "08 Aug 2026, 02:00 PM", "The complaint has been reviewed by the concerned department.", "completed"));
        c2.getTimeline().add(new TimelineEvent("In Progress", "09 Aug 2026, 10:00 AM", "Repair team has been dispatched to fix the light.", "active"));
        c2.getTimeline().add(new TimelineEvent("Resolution", "Pending", "The complaint will be marked resolved after the issue is addressed.", "pending"));
        complaints.put(c2.getId(), c2);

        // 3. CMP202600078 (Resolved)
        Complaint c3 = new Complaint(
            "CMP202600078",
            "Garbage collection issue",
            "Garbage collector has not visited the street for three days.",
            "Waste Management",
            "Resolved",
            "01 Aug 2026"
        );
        c3.getTimeline().add(new TimelineEvent("Complaint Submitted", "01 Aug 2026, 08:30 AM", "Your complaint has been successfully submitted.", "completed"));
        c3.getTimeline().add(new TimelineEvent("Under Review", "01 Aug 2026, 11:00 AM", "The complaint has been reviewed by the concerned department.", "completed"));
        c3.getTimeline().add(new TimelineEvent("Assigned to Officer", "01 Aug 2026, 03:00 PM", "Your complaint has been assigned to a concerned officer.", "completed"));
        c3.getTimeline().add(new TimelineEvent("Resolved", "02 Aug 2026, 04:00 PM", "The garbage has been cleared and area sanitized.", "completed"));
        complaints.put(c3.getId(), c3);

        // 4. CMP202600999 (Resolved)
        Complaint c4 = new Complaint(
            "CMP202600999",
            "Street Light Not Working",
            "Street light near the main road is not functioning.",
            "Electrical",
            "Resolved",
            "20 Aug 2026"
        );
        c4.getTimeline().add(new TimelineEvent("Complaint Submitted", "20 Aug 2026, 09:00 AM", "Your complaint has been successfully submitted.", "completed"));
        c4.getTimeline().add(new TimelineEvent("Under Review", "20 Aug 2026, 02:00 PM", "The complaint has been reviewed by the concerned department.", "completed"));
        c4.getTimeline().add(new TimelineEvent("In Progress", "21 Aug 2026, 10:00 AM", "Maintenance team dispatched.", "completed"));
        c4.getTimeline().add(new TimelineEvent("Resolved", "22 Aug 2026, 04:00 PM", "Street light bulb replaced and verified operational.", "completed"));
        complaints.put(c4.getId(), c4);
    }

    public Complaint getComplaint(String id) {
        return complaints.get(id);
    }

    public void saveComplaint(Complaint complaint) {
        complaints.put(complaint.getId(), complaint);
    }
}
