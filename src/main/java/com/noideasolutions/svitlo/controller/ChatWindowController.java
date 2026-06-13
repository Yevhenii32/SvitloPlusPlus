package com.noideasolutions.svitlo.controller;



import com.noideasolutions.svitlo.model.ChatMessage;
import com.noideasolutions.svitlo.service.TemporaryChatService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

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

    public static TemporaryChatService getChatService() {
        return chatService;
    }

    public void setChatData(int guestId, int hostId) {
        this.guestId = guestId;
        this.hostId = hostId;

        chatService.confirmRequestAndOpenChat(guestId, hostId);

        chatStatusLabel.setText("Чат активний");
        refreshMessages();
    }

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

    private void refreshMessages() {
        messagesListView.getItems().clear();

        for (ChatMessage message : chatService.getMessages()) {
            String line = "Користувач " + message.getSenderId() + ": " + message.getText();
            messagesListView.getItems().add(line);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}