package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.model.User;
import com.noideasolutions.svitlo.service.UserSession;
import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.service.HubService;
import java.util.List;
import javafx.scene.input.MouseEvent;
import com.noideasolutions.svitlo.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.io.IOException;

public class MainDashboardController {

    @FXML
    private Label userInfoLabel;

    @FXML
    private ToggleButton roleToggleButton;

    @FXML
    private ListView<String> hubsListView;

    private HubService hubService = new HubService();

    @FXML
    public void initialize() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            userInfoLabel.setText("Користувач: " + currentUser.getUsername() + " | Роль: " + currentUser.getRole());

            if ("HOST".equals(currentUser.getRole())) {
                roleToggleButton.setText("Режим хоста");
                roleToggleButton.setSelected(true);
            } else {
                roleToggleButton.setText("Режим гостя");
                roleToggleButton.setSelected(false);
            }
        }

        // Отримуємо реальні хаби з бази даних
        List<Hub> activeHubs = hubService.getAllActiveHubs();

        // Очищаємо список перед оновленням
        hubsListView.getItems().clear();

        if (activeHubs.isEmpty()) {
            hubsListView.getItems().add("Наразі немає доступних хабів зі світлом.");
        } else {
            for (Hub hub : activeHubs) {
                String hubInfo = String.format("%s (Вільних місць: %d/%d)",
                        hub.getTitle(), hub.getSlotsAvailable(), hub.getSlotsTotal());
                hubsListView.getItems().add(hubInfo);
            }
        }

        // Обробка подвійного кліку по списку
        hubsListView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                // Отримуємо ПОРЯДКОВИЙ НОМЕР рядка, по якому клікнули
                int selectedIndex = hubsListView.getSelectionModel().getSelectedIndex();

                // Перевіряємо, чи клік був по реальному хабу
                if (selectedIndex >= 0 && !activeHubs.isEmpty()) {
                    // Дістаємо справжній об'єкт Hub з нашого списку за цим індексом
                    Hub selectedHub = activeHubs.get(selectedIndex);
                    openHubDetails(event, selectedHub);
                }
            }
        });
    }

    //  Відкриває деталі і передає туди дані хабу
    private void openHubDetails(MouseEvent event, Hub hub) {
        // Конвертуємо подію миші (MouseEvent) у подію (ActionEvent)
        ActionEvent actionEvent = new ActionEvent(event.getSource(), event.getTarget());

        // Відкриваємо вікно і відловлюємо його контролер
        HubDetailsController controller = SceneSwitcher.switchToWithController(
                actionEvent,
                "/com/noideasolutions/svitlo/controller/HubDetails.fxml",
                "Деталі хабу: " + hub.getTitle()
        );

        // Якщо вікно успішно відкрилося, закидаємо туди наш об'єкт хабу
        if (controller != null) {
            controller.setHubData(hub);
        }
    }

    @FXML
    public void handleRoleSwitch(ActionEvent event) {
        if (roleToggleButton.isSelected()) {
            roleToggleButton.setText("Режим хоста");
        } else {
            roleToggleButton.setText("Режим гостя");
        }
    }

    @FXML
    private void handleCreateHubAction(ActionEvent event) {
        // Використовуємо SceneSwitcher для переходу
        SceneSwitcher.switchTo(event, "/com/noideasolutions/svitlo/controller/CreateHub.fxml", "Створення нового хабу");
    }

    @FXML
    public void handleLogoutAction(ActionEvent event) {
        UserSession.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/noideasolutions/svitlo/controller/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 350));
            stage.setTitle("Svitlo++ - Авторизація");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}