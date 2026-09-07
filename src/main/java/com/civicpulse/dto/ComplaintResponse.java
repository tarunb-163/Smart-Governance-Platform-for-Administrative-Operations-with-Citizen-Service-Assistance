package com.civicpulse.dto;

import com.civicpulse.model.TimelineEvent;
import java.util.List;

public class ComplaintResponse {

    private String complaintId;
    private String title;
    private String description;
    private String category;
    private String location;
    private String status;
    private String department;
    private String assignedOfficer;
    private String assignedOfficerDesignation;
    private String citizenName;
    private String submittedDate;
    private String createdAt;
    private String updatedAt;
    private String assignedAt;
    private String inProgressAt;
    private String resolvedAt;
    private String resolution;
    private String remarks;
    private List<TimelineEvent> timeline;
    private String imagePath;

    public ComplaintResponse() {}

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

    public String getTrackingId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAssignedOfficer() {
        return assignedOfficer;
    }

    public void setAssignedOfficer(String assignedOfficer) {
        this.assignedOfficer = assignedOfficer;
    }

    public String getAssignedOfficerDesignation() {
        return assignedOfficerDesignation;
    }

    public void setAssignedOfficerDesignation(String assignedOfficerDesignation) {
        this.assignedOfficerDesignation = assignedOfficerDesignation;
    }

    public String getCitizenName() {
        return citizenName;
    }

    public void setCitizenName(String citizenName) {
        this.citizenName = citizenName;
    }

    public String getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(String submittedDate) {
        this.submittedDate = submittedDate;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(String assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getInProgressAt() {
        return inProgressAt;
    }

    public void setInProgressAt(String inProgressAt) {
        this.inProgressAt = inProgressAt;
    }

    public String getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(String resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<TimelineEvent> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimelineEvent> timeline) {
        this.timeline = timeline;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}