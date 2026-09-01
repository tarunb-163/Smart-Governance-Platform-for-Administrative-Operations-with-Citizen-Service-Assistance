package com.civicpulse.dto;

import com.civicpulse.model.TimelineEvent;
import java.util.List;

public class ComplaintResponse {

    private String complaintId;
    private String title;
    private String description;
    private String category;
    private String status;
    private String submittedDate;
    private List<TimelineEvent> timeline;
    private String imagePath;

    public ComplaintResponse(String complaintId, String title,
                             String description, String category,
                             String status, String submittedDate,
                             List<TimelineEvent> timeline, String imagePath) {
        this.complaintId = complaintId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.submittedDate = submittedDate;
        this.timeline = timeline;
        this.imagePath = imagePath;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getStatus() {
        return status;
    }

    public String getSubmittedDate() {
        return submittedDate;
    }

    public List<TimelineEvent> getTimeline() {
        return timeline;
    }

    public String getImagePath() {
        return imagePath;
    }
}