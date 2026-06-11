package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.model.User;
import com.noideasolutions.svitlo.service.BookingService;
import com.noideasolutions.svitlo.service.UserSession;
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

    private final BookingService bookingService =
            new BookingService();

    public void setHubData(Hub hub) {
        this.currentHub = hub;

        titleLabel.setText(hub.getTitle());
        descriptionLabel.setText(hub.getDescription());

        updateSlotsInfo();
    }

    private void updateSlotsInfo() {
        slotsInfoLabel.setText(
                "Доступно місць: "
                        + currentHub.getSlotsAvailable()
                        + " з "
                        + currentHub.getSlotsTotal()
        );
    }

    @FXML
    private void handleBook(ActionEvent event) {

        String slotsStr = bookingSlotsField.getText();

        if (slotsStr == null || slotsStr.isBlank()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Увага",
                    "Введіть кількість місць."
            );
            return;
        }

        try {

            int requestedSlots =
                    Integer.parseInt(slotsStr);

            User currentUser =
                    UserSession.getInstance()
                            .getCurrentUser();

            if (currentUser == null) {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Помилка",
                        "Користувач не авторизований."
                );
                return;
            }

            bookingService.bookSlots(
                    currentHub,
                    currentUser.getId(),
                    requestedSlots
            );

            updateSlotsInfo();

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Успіх",
                    "Заброньовано "
                            + requestedSlots
                            + " місць."
            );

            SceneSwitcher.switchTo(
                    event,
                    "/com/noideasolutions/svitlo/controller/MainDashboard.fxml",
                    "Головне меню"
            );

        } catch (NumberFormatException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Помилка",
                    "Введіть число."
            );

        } catch (Exception e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Помилка бронювання",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneSwitcher.switchTo(
                event,
                "/com/noideasolutions/svitlo/controller/MainDashboard.fxml",
                "Головне меню"
        );
    }

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {
        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}