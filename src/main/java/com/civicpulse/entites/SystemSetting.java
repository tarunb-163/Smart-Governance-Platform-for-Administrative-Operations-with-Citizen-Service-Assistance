package com.civicpulse.entites;

import jakarta.persistence.*;

@Entity
@Table(name = "admin_settings")
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_name", nullable = false)
    private String applicationName = "CivicPulse";

    @Column(name = "default_complaint_status", nullable = false)
    private String defaultComplaintStatus = "Pending";

    public Long getId() { return id; }
    public String getApplicationName() { return applicationName; }
    public void setApplicationName(String applicationName) { this.applicationName = applicationName; }
    public String getDefaultComplaintStatus() { return defaultComplaintStatus; }
    public void setDefaultComplaintStatus(String defaultComplaintStatus) { this.defaultComplaintStatus = defaultComplaintStatus; }
}
