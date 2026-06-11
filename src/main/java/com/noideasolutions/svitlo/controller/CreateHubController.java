package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.model.Hub;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CreateHubController {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField latitudeField;

    @FXML
    private TextField longitudeField;

    @FXML
    private TextField slotsField;

    @FXML
    private void handleAddHub(ActionEvent event) {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        String latStr = latitudeField.getText();
        String lonStr = longitudeField.getText();
        String slotsStr = slotsField.getText();

        if (title.isEmpty() || latStr.isEmpty() || lonStr.isEmpty() || slotsStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Будь ласка, заповніть всі обов'язкові поля.");
            return;
        }

        try {
            double latitude = Double.parseDouble(latStr);
            double longitude = Double.parseDouble(lonStr);
            int slots = Integer.parseInt(slotsStr);
            int currentHostId = 1;

            Hub newHub = new Hub(currentHostId, title, description, latitude, longitude, slots);

            showAlert(Alert.AlertType.INFORMATION, "Успіх", "Хаб успішно створено: " + newHub.getTitle());

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Помилка формату", "Перевірте правильність вводу координат та кількості місць.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}