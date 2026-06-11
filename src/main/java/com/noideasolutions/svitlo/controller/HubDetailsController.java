package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class HubDetailsController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label slotsInfoLabel;

    @FXML
    private TextField bookingSlotsField;

    private Hub currentHub;

    public void setHubData(Hub hub) {
        this.currentHub = hub;
        titleLabel.setText(hub.getTitle());
        descriptionLabel.setText(hub.getDescription());
        slotsInfoLabel.setText("Доступно місць: " + hub.getSlotsAvailable() + " з " + hub.getSlotsTotal());
    }

    @FXML
    private void handleBook(ActionEvent event) {
        String slotsStr = bookingSlotsField.getText();

        if (slotsStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Увага", "Введіть кількість місць для бронювання.");
            return;
        }

        try {
            int requestedSlots = Integer.parseInt(slotsStr);

            if (requestedSlots <= 0 || requestedSlots > currentHub.getSlotsAvailable()) {
                showAlert(Alert.AlertType.ERROR, "Помилка", "Некоректна кількість місць.");
                return;
            }

            showAlert(Alert.AlertType.INFORMATION, "Успіх", "Ви успішно забронювали " + requestedSlots + " місць!");

            SceneSwitcher.switchTo(event, "/com/noideasolutions/svitlo/controller/MainDashboard.fxml", "Головне меню");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Введіть числове значення.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneSwitcher.switchTo(event, "/com/noideasolutions/svitlo/controller/MainDashboard.fxml", "Головне меню");
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}