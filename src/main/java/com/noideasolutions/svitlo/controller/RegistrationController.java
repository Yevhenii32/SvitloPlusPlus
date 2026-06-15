package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class RegistrationController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordButton;

    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisibleField;
    @FXML private Button toggleConfirmPasswordButton;

    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label statusLabel;
    @FXML private ImageView logoImageView;

    private final AuthService authService = new AuthService();
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("GUEST", "HOST");

        try {
            Image logo = new Image(getClass().getResourceAsStream(
                    "/com/noideasolutions/svitlo/images/logo.png"));
            logoImageView.setImage(logo);
        } catch (Exception e) {
            System.err.println("Логотип не знайдено.");
        }
    }

    @FXML
    private void handleTogglePassword() {
        if (!isPasswordVisible) {
            passwordVisibleField.setText(passwordField.getText());
            passwordVisibleField.setVisible(true);
            passwordField.setVisible(false);
            togglePasswordButton.setText("🙈");
            isPasswordVisible = true;
        } else {
            passwordField.setText(passwordVisibleField.getText());
            passwordField.setVisible(true);
            passwordVisibleField.setVisible(false);
            togglePasswordButton.setText("👁");
            isPasswordVisible = false;
        }
    }

    @FXML
    private void handleToggleConfirmPassword() {
        if (!isConfirmPasswordVisible) {
            confirmPasswordVisibleField.setText(confirmPasswordField.getText());
            confirmPasswordVisibleField.setVisible(true);
            confirmPasswordField.setVisible(false);
            toggleConfirmPasswordButton.setText("🙈");
            isConfirmPasswordVisible = true;
        } else {
            confirmPasswordField.setText(confirmPasswordVisibleField.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordVisibleField.setVisible(false);
            toggleConfirmPasswordButton.setText("👁");
            isConfirmPasswordVisible = false;
        }
    }

    @FXML
    public void handleRegisterAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = isPasswordVisible
                ? passwordVisibleField.getText()
                : passwordField.getText();
        String confirmPassword = isConfirmPasswordVisible
                ? confirmPasswordVisibleField.getText()
                : confirmPasswordField.getText();
        String roleString = roleComboBox.getValue();

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                confirmPassword == null || confirmPassword.trim().isEmpty() ||
                roleString == null) {
            statusLabel.setTextFill(Color.web("#ef4444"));
            statusLabel.setText("Будь ласка, заповніть усі поля.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            statusLabel.setTextFill(Color.web("#ef4444"));
            statusLabel.setText("Паролі не збігаються!");
            return;
        }

        try {
            authService.registerUser(username, password, roleString);
            statusLabel.setTextFill(Color.web("#22c55e"));
            statusLabel.setText("Акаунт успішно створено! Можете увійти.");
        } catch (com.noideasolutions.svitlo.exception.DuplicateUserException e) {
            statusLabel.setTextFill(Color.web("#ef4444"));
            statusLabel.setText(e.getMessage());
        } catch (IllegalArgumentException | com.noideasolutions.svitlo.exception.SvitloException e) {
            statusLabel.setTextFill(Color.web("#ef4444"));
            statusLabel.setText("Помилка: " + e.getMessage());
        }
    }

    @FXML
    public void handleBackToLoginAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(
                    "/com/noideasolutions/svitlo/controller/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 480, 680));
            stage.setTitle("Svitlo++ - Авторизація");
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setTextFill(Color.web("#ef4444"));
            statusLabel.setText("Помилка завантаження екрану входу.");
        }
    }
}