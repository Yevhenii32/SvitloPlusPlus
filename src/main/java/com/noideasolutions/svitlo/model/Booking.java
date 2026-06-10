package com.noideasolutions.svitlo.model;

import java.sql.Timestamp;

public class Booking {
    private int id;
    private int userId;
    private int hubId;
    private int bookedSlots;
    private Timestamp createdAt;
    private String hubTitle;

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

    public String getHubTitle() { return hubTitle; }
    public void setHubTitle(String hubTitle) { this.hubTitle = hubTitle; }

    // Зручний метод для формування фінального рядка
    public String getFullHubDescription() {
        if (hubTitle == null) {
            return "Хаб №" + hubId + ", " + bookedSlots + " місць";
        }
        return hubTitle + ", " + bookedSlots + " місця";
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", userId=" + userId +
                ", hubId=" + hubId +
                ", hubTitle='" + hubTitle + '\'' +
                ", slots=" + bookedSlots +
                '}';
    }
}