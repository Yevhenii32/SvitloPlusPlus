package com.noideasolutions.svitlo.model;

import java.time.LocalDateTime;

public class RadarRequest {

    private int guestId;
    private double latitude;
    private double longitude;
    private double radiusKm;
    private boolean active;
    private LocalDateTime createdAt;

    public RadarRequest(int guestId, double latitude, double longitude, double radiusKm) {
        this.guestId = guestId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusKm = radiusKm;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public int getGuestId() {
        return guestId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getRadiusKm() {
        return radiusKm;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void deactivate() {
        this.active = false;
    }
}