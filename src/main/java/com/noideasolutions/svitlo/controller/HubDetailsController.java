package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.dao.UserDAO;
import com.noideasolutions.svitlo.exception.SvitloException;
import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.model.User;
import com.noideasolutions.svitlo.service.BookingService;
import com.noideasolutions.svitlo.service.ReportService;
import com.noideasolutions.svitlo.service.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;

import java.util.Optional;

public class HubDetailsController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label hostInfoLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label slotsInfoLabel;

    // НОВІ ПОЛЯ ДЛЯ ЗВ'ЯЗКУ З ОНОВЛЕНИМ FXML
    @FXML
    private Label coordinatesLabel;

    @FXML
    private Label wifiLabel;

    @FXML
    private Label generatorLabel;

    @FXML
    private Label petsLabel;

    @FXML
    private TextField bookingSlotsField;

    @FXML
    private Button messageHostButton;

    private MainDashboardController mainDashboardController;

    private Hub currentHub;

    private final BookingService bookingService = new BookingService();
    private final ReportService reportService = new ReportService();
    private final UserDAO userDAO = new UserDAO();

    public void setHubData(Hub hub) {
        this.currentHub = hub;

        titleLabel.setText(hub.getTitle());
        descriptionLabel.setText(hub.getDescription() != null && !hub.getDescription().isEmpty()
                ? hub.getDescription() : "Немає опису");

        // 1. Заповнюємо координати хабу
        if (coordinatesLabel != null) {
            coordinatesLabel.setText(String.format("📍 Координати: %.4f, %.4f", hub.getLatitude(), hub.getLongitude()));
        }

        // 2. Формуємо інформацію про зручності (перенесено з showHubPopup)
        if (wifiLabel != null) {
            wifiLabel.setText("• Інтернет Wi-Fi: " + (hub.isHasWifi() ? "✅ Є" : "❌ Немає"));
        }
        if (generatorLabel != null) {
            generatorLabel.setText("• Генератор: " + (hub.isHasGenerator() ? "✅ Є" : "❌ Немає"));
        }
        if (petsLabel != null) {
            petsLabel.setText("• Можна з тваринами: " + (hub.isAllowsPets() ? "✅ Дозволено" : "❌ Заборонено"));
        }

        // 3. Інформація про власника
        User host = userDAO.findById(hub.getHostId());
        if (host != null) {
            hostInfoLabel.setText(String.format("Власник: %s (⭐ %.1f)", host.getUsername(), host.getRating()));
        } else {
            hostInfoLabel.setText("Власник: Невідомо");
        }

        updateSlotsInfo();

        if (messageHostButton != null) {
            messageHostButton.setDisable(true);
        }
    }

    private void updateSlotsInfo() {
        slotsInfoLabel.setText("Доступно місць: " + currentHub.getSlotsAvailable() + " з " + currentHub.getSlotsTotal());
    }

    @FXML
    private void handleBook(ActionEvent event) {
        String slotsStr = bookingSlotsField.getText();

        if (slotsStr == null || slotsStr.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Увага", "Введіть кількість місць.");
            return;
        }

        try {
            int requestedSlots = Integer.parseInt(slotsStr);
            User currentUser = UserSession.getInstance().getCurrentUser();

            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Помилка", "Користувач не авторизований.");
                return;
            }

            // 1. Проводимо бронювання
            bookingService.bookSlots(currentHub, currentUser.getId(), requestedSlots);
            updateSlotsInfo();

            // 2. ОНОВЛЮЄМО ДАНІ НА МАПІ ТА В ListView (на задньому плані)
            if (mainDashboardController != null) {
                mainDashboardController.updateDashboardData();
            }

            ChatWindowController.getChatService()
                    .confirmRequestAndOpenChat(currentUser.getId(), currentHub.getHostId());

            messageHostButton.setDisable(false);

            showAlert(Alert.AlertType.INFORMATION, "Успіх",
                    "Заброньовано " + requestedSlots + " місць. Тепер можна написати хосту.");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Введіть число.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Помилка бронювання", e.getMessage());
        }
    }

    @FXML
    private void handleOpenChat(ActionEvent event) {
        try {
            User currentUser = UserSession.getInstance().getCurrentUser();

            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Помилка", "Користувач не авторизований.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/noideasolutions/svitlo/controller/ChatWindow.fxml"
            ));

            Parent root = loader.load();

            ChatWindowController controller = loader.getController();
            controller.setChatData(currentUser.getId(), currentHub.getHostId());

            Stage stage = new Stage();
            stage.setTitle("Чат з хостом");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося відкрити чат: " + e.getMessage());
        }
    }

    @FXML
    private void handleReport(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Скарга на хаб");
        dialog.setHeaderText("Повідомити про порушення:\n\"" + currentHub.getTitle() + "\"");
        dialog.setContentText("Опишіть причину скарги:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(reason -> {
            if (reason.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Увага", "Причина скарги не може бути порожньою!");
                return;
            }

            try {
                int reporterId = UserSession.getInstance().getCurrentUser().getId();
                reportService.submitReport(reporterId, currentHub, reason);

                showAlert(Alert.AlertType.INFORMATION, "Скаргу надіслано", "Вашу скаргу зафіксовано.");

                // Оновлюємо дані на головному екрані на всякий випадок
                if (mainDashboardController != null) {
                    mainDashboardController.updateDashboardData();
                }

                // Замість SceneSwitcher просто закриваємо це вікно, бо мапа вже відкрита ззаду
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();

            } catch (SvitloException | IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Помилка сервера", e.getMessage());
            }
        });
    }

    @FXML
    private void handleBack(ActionEvent event) {
        // Просто закриваємо це вікно, і користувач одразу бачить активну карту під ним
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleOpenHostProfileAction(MouseEvent event) {
        if (currentHub == null) return;

        try {
            // 1. Завантажуємо FXML нового вікна профілю хоста
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/noideasolutions/svitlo/controller/HostProfile.fxml"
            ));
            Parent root = loader.load();

            // 2. Дістаємо його контролер та передаємо туди ID власника хабу
            HostProfileController controller = loader.getController();
            if (controller != null) {
                controller.setHostId(currentHub.getHostId()); // Передаємо hostId з моделі Hub
            }

            // 3. Створюємо нову сцену та stage
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Профіль хоста");

            // Робимо вікно модальним, щоб користувач фокусувався на профілі
            Stage ownerStage = (Stage) hostInfoLabel.getScene().getWindow();
            stage.initOwner(ownerStage);
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.show();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося відкрити профіль хоста: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setMainDashboardController(MainDashboardController mainDashboardController) {
        this.mainDashboardController = mainDashboardController;
    }
}