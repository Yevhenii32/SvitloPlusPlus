package com.noideasolutions.svitlo.model;

import java.sql.Timestamp;

public class Booking {
    private int id;
    private int userId;
    private int hubId;
    private int bookedSlots;
    private Timestamp createdAt;

    public Booking() {}

    public Booking(int userId, int hubId, int bookedSlots) {
        this.userId = userId;
        this.hubId = hubId;
        this.bookedSlots = bookedSlots;
    }

    // Геттери та Сеттери
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getHubId() { return hubId; }
    public void setHubId(int hubId) { this.hubId = hubId; }

    public int getBookedSlots() { return bookedSlots; }
    public void setBookedSlots(int bookedSlots) { this.bookedSlots = bookedSlots; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", userId=" + userId +
                ", hubId=" + hubId +
                ", slots=" + bookedSlots +
                '}';
    }
}