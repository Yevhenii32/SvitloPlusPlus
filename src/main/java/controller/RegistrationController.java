package com.noideasolutions.svitlo.controller;

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

public class RegistrationController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label statusLabel;

    @FXML
    public void handleRegisterAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // 1. Перевірка на порожні поля
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                confirmPassword == null || confirmPassword.trim().isEmpty()) {

            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Будь ласка, заповніть усі поля.");
            return;
        }

        // 2. Перевірка на збіг паролів
        if (!password.equals(confirmPassword)) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Паролі не збігаються!");
            return;
        }

        // TODO: Збереження користувача в БД через DAO
        System.out.println("Registration attempt for user: " + username);

        statusLabel.setTextFill(Color.GREEN);
        statusLabel.setText("Акаунт успішно створено! (Mock)");
    }

    @FXML
    public void handleBackToLoginAction(ActionEvent event) {
        try {
            // Завантажуємо екран логіну
            Parent root = FXMLLoader.load(getClass().getResource("/com/noideasolutions/svitlo/controller/Login.fxml"));

            // Отримуємо поточне вікно (Stage) з події натискання кнопки
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Встановлюємо нову сцену
            stage.setScene(new Scene(root, 400, 350));
            stage.setTitle("Svitlo++ - Авторизація");

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Помилка завантаження екрану входу.");
        }
    }
}