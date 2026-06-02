package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.model.User;
import com.noideasolutions.svitlo.service.UserSession;
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

        hubsListView.getItems().addAll("Хаб 1 (0.5 км)", "Хаб 2 (1.2 км)", "Хаб 3 (2.0 км)");
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