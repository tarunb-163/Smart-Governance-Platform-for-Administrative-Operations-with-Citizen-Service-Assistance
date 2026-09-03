package com.civicpulse.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class TimelineEvent {
    private String title;
    private String date;
    private String description;
    private String status; // completed, active, pending

    public TimelineEvent() {}

    public TimelineEvent(String title, String date, String description, String status) {
        this.title = title;
        this.date = date;
        this.description = description;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
