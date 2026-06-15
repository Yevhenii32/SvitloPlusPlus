package com.noideasolutions.svitlo.controller;



import com.noideasolutions.svitlo.model.ChatMessage;
import com.noideasolutions.svitlo.service.TemporaryChatService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Контролер для керування вікном тимчасового чату між гостем та хостом (власником хабу).
 * Забезпечує надсилання, отримання та динамічне оновлення текстових повідомлень.
 */
public class ChatWindowController {

    @FXML
    private ListView<String> messagesListView;

    @FXML
    private TextField messageField;

    @FXML
    private Label chatStatusLabel;

    private static final TemporaryChatService chatService = new TemporaryChatService();

    private int guestId;
    private int hostId;

    /**
     * Повертає поточний екземпляр сервісу тимчасового чату.
     */
    public static TemporaryChatService getChatService() {
        return chatService;
    }

    /**
     * Ініціалізує чат необхідними даними учасників, підтверджує запит
     * та завантажує наявну історію листування.
     */
    public void setChatData(int guestId, int hostId) {
        this.guestId = guestId;
        this.hostId = hostId;

        chatService.confirmRequestAndOpenChat(guestId, hostId);

        chatStatusLabel.setText("Чат активний");
        refreshMessages();
    }

    /**
     * Обробник події натискання кнопки надсилання повідомлення або клавіші Enter.
     * Зчитує текст, передає його в сервіс та оновлює інтерфейс.
     */
    @FXML
    private void handleSendMessage() {
        String text = messageField.getText();

        try {
            chatService.sendMessage(guestId, hostId, text);
            messageField.clear();
            refreshMessages();
        } catch (Exception e) {
            showAlert("Помилка", e.getMessage());
        }
    }

    /**
     * Очищає поточний список повідомлень на екрані та повністю
     * перезавантажує актуальну історію з сервісу чату.
     */
    private void refreshMessages() {
        messagesListView.getItems().clear();

        for (ChatMessage message : chatService.getMessages()) {
            String line = "Користувач " + message.getSenderId() + ": " + message.getText();
            messagesListView.getItems().add(line);
        }
    }

    /**
     * Допоміжний метод для виведення модального вікна з повідомленням про помилку.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}