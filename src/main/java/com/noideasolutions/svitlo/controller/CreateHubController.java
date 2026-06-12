package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.service.HubService;
import com.noideasolutions.svitlo.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox; // ДОДАНО ІМПОРТ
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
    private CheckBox wifiCheckBox;
    @FXML
    private CheckBox generatorCheckBox;
    @FXML
    private CheckBox petsCheckBox;

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

            // Зчитуємо стан чекбоксів (true або false)
            boolean hasWifi = wifiCheckBox != null && wifiCheckBox.isSelected();
            boolean hasGenerator = generatorCheckBox != null && generatorCheckBox.isSelected();
            boolean allowsPets = petsCheckBox != null && petsCheckBox.isSelected();

            // ТЕПЕР ПЕРЕДАЄМО ВСІ 8 ПАРАМЕТРІВ У СЕРВІС
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
                // Автоматично повертаємо користувача на Головний Дашборд
                SceneSwitcher.switchTo(event, "/com/noideasolutions/svitlo/controller/MainDashboard.fxml", "Головне меню");
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

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBackAction(ActionEvent event) {
        SceneSwitcher.switchTo(event, "/com/noideasolutions/svitlo/controller/MainDashboard.fxml", "Головна панель");
    }
}