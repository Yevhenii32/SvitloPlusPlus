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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField; // Додано для відображення пароля

    @FXML
    private Button togglePasswordButton; // Кнопка "Око"

    @FXML
    private ImageView logoImageView; // Контейнер для логотипу

    @FXML
    private Label errorLabel;

    private final AuthService authService = new AuthService();
    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
        // Завантажуємо логотип додатка при старті екрану
        try {
            Image logo = new Image(getClass().getResourceAsStream("/com/noideasolutions/svitlo/images/logo.png"));
            logoImageView.setImage(logo);
        } catch (Exception e) {
            System.err.println("Попередження: Логотип за шляхом /com/noideasolutions/svitlo/images/logo.png не знайдено.");
        }
    }

    @FXML
    private void handleTogglePassword() {
        if (!isPasswordVisible) {
            // Переносимо текст із точок у звичайне поле і показуємо його
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
            togglePasswordButton.setText("🙈"); // Змінюємо іконку
            isPasswordVisible = true;
        } else {
            // Переносимо текст назад у поле з точками і ховаємо звичайне
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordTextField.setVisible(false);
            togglePasswordButton.setText("👁");
            isPasswordVisible = false;
        }
    }

    @FXML
    public void handleLoginAction(ActionEvent event) {
        String username = usernameField.getText();

        // Зчитуємо пароль залежно від того, яке поле зараз активне (видиме)
        String password = isPasswordVisible ? passwordTextField.getText() : passwordField.getText();

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            errorLabel.setTextFill(Color.web("#ef4444")); // Фірмовий червоний під новий макет
            errorLabel.setText("Будь ласка, заповніть усі поля.");
            return;
        }

        try {
            User user = authService.login(username, password);

            if (user != null) {
                UserSession.getInstance().setCurrentUser(user);

                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/com/noideasolutions/svitlo/controller/MainDashboard.fxml"));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root, 1200, 800));

                    stage.centerOnScreen();

                    stage.setTitle("Svitlo++ - Головна панель");
                } catch (IOException e) {
                    e.printStackTrace();
                    errorLabel.setTextFill(Color.web("#ef4444"));
                    errorLabel.setText("Помилка завантаження головного екрану.");
                }
            }
        } catch (com.noideasolutions.svitlo.exception.SvitloException e) {
            errorLabel.setTextFill(Color.web("#ef4444"));
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void handleGoToRegistrationAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/noideasolutions/svitlo/controller/Registration.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // Збільшено розмір вікна реєстрації відповідно до нового макету
            stage.setScene(new Scene(root, 480, 760));
            stage.setTitle("Svitlo++ - Реєстрація");
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setTextFill(Color.web("#ef4444"));
            errorLabel.setText("Помилка завантаження екрану реєстрації.");
        }
    }
}