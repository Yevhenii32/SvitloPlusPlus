package com.noideasolutions.svitlo.model;



import java.time.LocalDateTime;

public class ChatMessage {
    private int senderId;
    private int receiverId;
    private String text;
    private LocalDateTime createdAt;

    public ChatMessage(int senderId, int receiverId, String text) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.createdAt = LocalDateTime.now();
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}