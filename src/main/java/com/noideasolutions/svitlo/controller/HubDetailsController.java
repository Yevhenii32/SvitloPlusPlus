package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.dao.UserDAO;
import com.noideasolutions.svitlo.exception.SvitloException;
import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.model.User;
import com.noideasolutions.svitlo.service.BookingService;
import com.noideasolutions.svitlo.service.ReportService;
import com.noideasolutions.svitlo.service.UserSession;
import com.noideasolutions.svitlo.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

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

    @FXML
    private TextField bookingSlotsField;

    @FXML
    private Button messageHostButton;

    private Hub currentHub;

    private final BookingService bookingService = new BookingService();
    private final ReportService reportService = new ReportService();
    private final UserDAO userDAO = new UserDAO();

    public void setHubData(Hub hub) {
        this.currentHub = hub;

        titleLabel.setText(hub.getTitle());
        descriptionLabel.setText(hub.getDescription());

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

            bookingService.bookSlots(currentHub, currentUser.getId(), requestedSlots);
            updateSlotsInfo();

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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}