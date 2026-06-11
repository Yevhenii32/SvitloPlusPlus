package com.noideasolutions.svitlo.model;

public class PartnerReward {
    private int id;
    private String partnerName;   // Назва компанії партнерів
    private String title;         // Назва товару
    private String description;   // Опис товару
    private int costInPoints;     // Вартість у бонусних балах

    public PartnerReward() {
    }

    public PartnerReward(int id, String title, String partnerName, int costInPoints) {
        this.id = id;
        this.title = title;
        this.partnerName = partnerName;
        this.costInPoints = costInPoints;
    }

    // Геттери та сеттери
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCostInPoints() { return costInPoints; }
    public void setCostInPoints(int costInPoints) { this.costInPoints = costInPoints; }
}