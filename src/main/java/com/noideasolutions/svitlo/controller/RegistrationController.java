package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class RegistrationController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Label statusLabel;

    private AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("GUEST", "HOST");
    }

    @FXML
    public void handleRegisterAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String roleString = roleComboBox.getValue();

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                confirmPassword == null || confirmPassword.trim().isEmpty() ||
                roleString == null) {

            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Будь ласка, заповніть усі поля.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Паролі не збігаються!");
            return;
        }

        boolean isRegistered = authService.registerUser(username, password, roleString);

        if (isRegistered) {
            statusLabel.setTextFill(Color.GREEN);
            statusLabel.setText("Акаунт успішно створено! Можете увійти.");
        } else {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Помилка реєстрації. Можливо, логін вже зайнятий.");
        }
    }

    @FXML
    public void handleBackToLoginAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/noideasolutions/svitlo/controller/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 350));
            stage.setTitle("Svitlo++ - Авторизація");
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Помилка завантаження екрану входу.");
        }
    }
}