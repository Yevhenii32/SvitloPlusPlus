package com.noideasolutions.svitlo.model;

import java.time.LocalDateTime;

public class Report {
    private int id;
    private int reporterId; // ID гостя, який скаржиться
    private int hubId;      // ID хабу, на який скаржаться
    private String reason;  // Причина скарги
    private LocalDateTime timestamp;

    public Report() {}

    public Report(int id, int reporterId, int hubId, String reason) {
        this.id = id;
        this.reporterId = reporterId;
        this.hubId = hubId;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    // Геттери та сеттери
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReporterId() { return reporterId; }
    public void setReporterId(int reporterId) { this.reporterId = reporterId; }

    public int getHubId() { return hubId; }
    public void setHubId(int hubId) { this.hubId = hubId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}