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

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    public void handleLoginAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validation for empty fields
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {

            errorLabel.setTextFill(Color.RED);
            errorLabel.setText("Будь ласка, заповніть усі поля.");
            return;
        }

        // TODO: Will be replaced with ViewModel and Database authentication later
        System.out.println("Login attempt with username: " + username);

        // Temporary mock for successful login verification
        errorLabel.setTextFill(Color.GREEN);
        errorLabel.setText("Успішний вхід! (Mock)");
    }

    @FXML
    public void handleGoToRegistrationAction(ActionEvent event) {
        try {
            // Load the registration screen from resources
            Parent root = FXMLLoader.load(getClass().getResource("/com/noideasolutions/svitlo/controller/Registration.fxml"));

            // Get the current stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Adjust scene size for the registration form (it has more fields)
            stage.setScene(new Scene(root, 400, 420));
            stage.setTitle("Svitlo++ - Реєстрація");

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setTextFill(Color.RED);
            errorLabel.setText("Помилка завантаження екрану реєстрації.");
        }
    }
}