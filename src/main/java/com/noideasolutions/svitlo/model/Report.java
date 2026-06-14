package com.noideasolutions.svitlo.model;

import java.time.LocalDateTime;

public class Report {
    private int id;
    private int reporterId;
    private Integer hubId;           // тепер Integer (може бути null)
    private Integer reportedUserId;  // тепер Integer (може бути null)
    private String reason;
    private LocalDateTime timestamp;

    public Report() {}

    // Конструктор для скарги на ХАБ (reportedUserId буде null)
    public Report(int id, int reporterId, Integer hubId, String reason) {
        this.id = id;
        this.reporterId = reporterId;
        this.hubId = hubId;
        this.reportedUserId = null;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    // Конструктор для скарги на КОРИСТУВАЧА (hubId буде null)
    public Report(int id, int reporterId, String reason, Integer reportedUserId) {
        this.id = id;
        this.reporterId = reporterId;
        this.hubId = null;
        this.reportedUserId = reportedUserId;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    // Геттери та сеттери
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReporterId() { return reporterId; }
    public void setReporterId(int reporterId) { this.reporterId = reporterId; }

    public Integer getHubId() { return hubId; }
    public void setHubId(Integer hubId) { this.hubId = hubId; }

    public Integer getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(Integer reportedUserId) { this.reportedUserId = reportedUserId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}