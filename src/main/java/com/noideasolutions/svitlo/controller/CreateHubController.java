package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.service.HubService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateHubController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private TextField slotsField;
    @FXML private CheckBox wifiCheckBox;
    @FXML private CheckBox generatorCheckBox;
    @FXML private CheckBox petsCheckBox;

    private HubService hubService = new HubService();

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

            boolean hasWifi = wifiCheckBox != null && wifiCheckBox.isSelected();
            boolean hasGenerator = generatorCheckBox != null && generatorCheckBox.isSelected();
            boolean allowsPets = petsCheckBox != null && petsCheckBox.isSelected();

            boolean isCreated = hubService.createHub(
                    title,
                    description,
                    latitude,
                    longitude,
                    slots,
                    hasWifi,
                    hasGenerator,
                    allowsPets
            );

            if (isCreated) {
                showAlert(Alert.AlertType.INFORMATION, "Успіх", "Хаб успішно створено: " + title);

                // Замість SceneSwitcher просто закриваємо поточне вікно створення.
                // Оскільки головний дашборд чекає на фоні, користувач знову побачить його.
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося створити хаб. Перевірте логі консолі.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Помилка формату", "Перевірте правильність вводу координат та кількості місць.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося зберегти хаб: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackAction(ActionEvent event) {
        // Прибираємо SceneSwitcher. switchTo плодив дублікат головного меню.
        // Просто закриваємо це вікно, і фокус автоматично повернеться на головний дашборд.
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}