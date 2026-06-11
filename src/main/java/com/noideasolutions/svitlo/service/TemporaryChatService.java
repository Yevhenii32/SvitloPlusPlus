package com.noideasolutions.svitlo.service;



import com.noideasolutions.svitlo.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class TemporaryChatService {

    private final List<ChatMessage> messages = new ArrayList<>();
    private boolean chatActive = false;
    private int guestId;
    private int hostId;

    public void confirmRequestAndOpenChat(int guestId, int hostId) {
        if (guestId <= 0 || hostId <= 0) {
            throw new IllegalArgumentException("User IDs must be positive");
        }

        this.guestId = guestId;
        this.hostId = hostId;
        this.chatActive = true;
    }

    public void closeChat() {
        chatActive = false;
        messages.clear();
    }

    public void sendMessage(int senderId, int receiverId, String text) {
        if (!chatActive) {
            throw new IllegalStateException("Chat is not active");
        }

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        if (!isAllowedParticipant(senderId) || !isAllowedParticipant(receiverId)) {
            throw new SecurityException("User is not allowed to use this chat");
        }

        messages.add(new ChatMessage(senderId, receiverId, text));
    }

    public List<ChatMessage> getMessages() {
        return new ArrayList<>(messages);
    }

    private boolean isAllowedParticipant(int userId) {
        return userId == guestId || userId == hostId;
    }
}
