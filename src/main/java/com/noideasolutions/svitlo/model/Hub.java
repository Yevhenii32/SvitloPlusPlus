package com.noideasolutions.svitlo.model;

import java.sql.Timestamp;

public class Hub {
    private int id;
    private int ownerId;
    private int reportCount = 0; // За замовчуванням 0 скарг
    private int hostId; // ID користувача, який створив хаб
    private String title;
    private String description;
    private double latitude;
    private double longitude;
    private int slotsTotal;
    private int slotsAvailable;
    private boolean isActive;
    private Timestamp createdAt;


    public Hub() {}

    public Hub(int hostId, String title, String description, double latitude, double longitude, int slotsTotal) {
        this.hostId = hostId;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.slotsTotal = slotsTotal;
        this.slotsAvailable = slotsTotal; // При створенні всі слоти вільні
        this.isActive = true;
    }

    // Геттери та Сеттери
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHostId() { return hostId; }
    public void setHostId(int hostId) { this.hostId = hostId; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId ;}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getSlotsTotal() { return slotsTotal; }
    public void setSlotsTotal(int slotsTotal) { this.slotsTotal = slotsTotal; }

    public int getSlotsAvailable() { return slotsAvailable; }
    public void setSlotsAvailable(int slotsAvailable) { this.slotsAvailable = slotsAvailable; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getReportCount() { return reportCount; }
    public void setReportCount(int reportCount) { this.reportCount = reportCount; }

    @Override
    public String toString() {
        return "Hub{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", available=" + slotsAvailable + "/" + slotsTotal +
                '}';
    }
}