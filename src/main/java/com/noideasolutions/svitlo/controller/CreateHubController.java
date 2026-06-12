package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.service.HubService;
import com.noideasolutions.svitlo.service.UserSession;
import com.noideasolutions.svitlo.util.SceneSwitcher;
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

            // Беремо реальний ID користувача (хоста), який зараз залогінений
            int currentHostId = UserSession.getInstance().getCurrentUser().getId();

            Hub newHub = new Hub(currentHostId, title, description, latitude, longitude, slots);

            hubService.createHub(title, description, latitude, longitude, slots);

            showAlert(Alert.AlertType.INFORMATION, "Успіх", "Хаб успішно створено: " + newHub.getTitle());

            // Автоматично повертаємо користувача на Головний Дашборд, щоб він побачив свій новий хаб
            SceneSwitcher.switchTo(event, "/com/noideasolutions/svitlo/controller/MainDashboard.fxml", "Головне меню");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Помилка формату", "Перевірте правильність вводу координат та кількості місць.");
        } catch (Exception e) {
            // Відловлюємо помилки бази даних (наприклад, якщо впаде з'єднання з Neon)
            showAlert(Alert.AlertType.ERROR, "Помилка БД", "Не вдалося зберегти хаб: " + e.getMessage());
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
        // Повертаємо користувача на головну панель
        SceneSwitcher.switchTo(event, "/com/noideasolutions/svitlo/controller/MainDashboard.fxml", "Головна панель");
    }
}