package com.noideasolutions.svitlo.service;



import com.noideasolutions.svitlo.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервіс для ведення тимчасових чатів між гостями та хостами в оперативній пам'яті.
 * Забезпечує перевірку прав доступу учасників.
 */
public class TemporaryChatService {

    private final List<ChatMessage> messages = new ArrayList<>();
    private boolean chatActive = false;
    private int guestId;
    private int hostId;

    /**
     * Підтверджує запит, пов'язує ідентифікатори учасників та активує сесію чату.
     */
    public void confirmRequestAndOpenChat(int guestId, int hostId) {
        if (guestId <= 0 || hostId <= 0) {
            throw new IllegalArgumentException("User IDs must be positive");
        }

        this.guestId = guestId;
        this.hostId = hostId;
        this.chatActive = true;
    }

    /**
     * Деактивує поточну сесію чату та повністю очищає історію повідомлень з пам'яті.
     */
    public void closeChat() {
        chatActive = false;
        messages.clear();
    }

    /**
     * Валідує та додає нове повідомлення до списку, якщо сесія активна, а відправник і отримувач є учасниками чату.
     */
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

    /**
     * Повертає незалежну копію списку всіх повідомлень для відображення в UI.
     */
    public List<ChatMessage> getMessages() {
        return new ArrayList<>(messages);
    }

    /**
     * Перевіряє, чи належить вказаний ідентифікатор одному з дозволених учасників поточної сесії.
     */
    private boolean isAllowedParticipant(int userId) {
        return userId == guestId || userId == hostId;
    }
}
