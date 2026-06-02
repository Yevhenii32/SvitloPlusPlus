package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.model.User;
import com.noideasolutions.svitlo.service.AuthService;
import com.noideasolutions.svitlo.service.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private AuthService authService = new AuthService();

    @FXML
    public void handleLoginAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {

            errorLabel.setTextFill(Color.RED);
            errorLabel.setText("Будь ласка, заповніть усі поля.");
            return;
        }

        User user = authService.login(username, password);

        if (user != null) {
            UserSession.getInstance().setCurrentUser(user);

            try {
                Parent root = FXMLLoader.load(getClass().getResource("/com/noideasolutions/svitlo/controller/MainDashboard.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root, 900, 600));
                stage.setTitle("Svitlo++ - Головна панель");
            } catch (IOException e) {
                e.printStackTrace();
                errorLabel.setTextFill(Color.RED);
                errorLabel.setText("Помилка завантаження головного екрану.");
            }
        } else {
            errorLabel.setTextFill(Color.RED);
            errorLabel.setText("Помилка: Неправильний логін або пароль!");
        }
    }

    @FXML
    public void handleGoToRegistrationAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/noideasolutions/svitlo/controller/Registration.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 480));
            stage.setTitle("Svitlo++ - Реєстрація");
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setTextFill(Color.RED);
            errorLabel.setText("Помилка завантаження екрану реєстрації.");
        }
    }
}