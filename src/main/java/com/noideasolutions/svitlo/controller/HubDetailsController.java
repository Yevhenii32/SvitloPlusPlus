package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.service.ReportService;
import com.noideasolutions.svitlo.service.UserSession;
import com.noideasolutions.svitlo.util.SceneSwitcher;
import com.noideasolutions.svitlo.exception.SvitloException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

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

    // 🔥 Підключаємо твій готовий сервіс скарг
    private final ReportService reportService = new ReportService();

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

    // 🔥 НОВИЙ МЕТОД: Обробка натискання на кнопку "Поскаржитись"
    @FXML
    private void handleReport(ActionEvent event) {
        // Створюємо стандартне діалогове вікно JavaFX з текстовим полем
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Скарга на хаб");
        dialog.setHeaderText("Повідомити про порушення або неактуальність хабу:\n\"" + currentHub.getTitle() + "\"");
        dialog.setContentText("Опишіть причину скарги:");

        // Показуємо вікно і чекаємо на введення тексту користувачем
        Optional<String> result = dialog.showAndWait();

        // Якщо користувач натиснув "ОК" і ввів текст
        result.ifPresent(reason -> {
            if (reason.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Увага", "Причина скарги не може бути порожньою!");
                return;
            }

            try {
                // Дістаємо ID поточного користувача, який залишає скаргу
                int reporterId = UserSession.getInstance().getCurrentUser().getId();

                // Викликаємо твою бізнес-логіку з ReportService
                reportService.submitReport(reporterId, currentHub, reason);

                showAlert(Alert.AlertType.INFORMATION, "Скаргу надіслано",
                        "Дякуємо! Вашу скаргу успішно зафіксовано системою модерації.");

                // Повертаємо користувача на головний екран.
                // Якщо це була 3-тя скарга, хаб автоматично зникне зі списку, бо твій сервіс вимкне його активність!
                SceneSwitcher.switchTo(event, "/com/noideasolutions/svitlo/controller/MainDashboard.fxml", "Головне меню");

            } catch (SvitloException | IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Помилка сервера", e.getMessage());
            }
        });
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