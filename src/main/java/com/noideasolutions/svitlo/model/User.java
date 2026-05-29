package com.noideasolutions.svitlo.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String role; // "HOST" або "GUEST"
    private double rating;
    private int bonusPoints;
    private int complaintsCount;
    private boolean isBlocked;

    // Порожній конструктор
    public User() {}

    // Конструктор для створення нового користувача
    public User(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.rating = 5.00; // Базовий рейтинг
        this.bonusPoints = 0;
        this.complaintsCount = 0;
        this.isBlocked = false;
    }

    // Геттери та Сеттери
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getBonusPoints() { return bonusPoints; }
    public void setBonusPoints(int bonusPoints) { this.bonusPoints = bonusPoints; }

    public int getComplaintsCount() { return complaintsCount; }
    public void setComplaintsCount(int complaintsCount) { this.complaintsCount = complaintsCount; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", rating=" + rating +
                '}';
    }
}