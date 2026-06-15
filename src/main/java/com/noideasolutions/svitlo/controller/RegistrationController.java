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

/**
 * Контролер для керування екраном реєстрації нових користувачів у системі "Svitlo++".
 * Забезпечує збір та первинну валідацію реєстраційних даних (збіг паролів, заповненість полів),
 * підтримує інтерактивний механізм приховування/відображення символів паролів
 * та взаємодіє з AuthService для створення облікових записів у базі даних із ролями GUEST або HOST.
 */
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

    /**
     * Метод ініціалізації JavaFX. Автоматично викликається після завантаження FXML-файлу.
     * Заповнює випадаючий список ролей та завантажує логотип програми.
     */
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

    /**
     * Обробник події для перемикання видимості основного пароля.
     * Переносить введене значення між PasswordField та звичайним TextField
     * і змінює текстовий маркер кнопки (емодзі).
     */
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

    /**
     * Обробник події для перемикання видимості поля підтвердження пароля.
     * Працює аналогічно методу handleTogglePassword, але для другої пари полів.
     */
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

    /**
     * Обробник події кліку на кнопку "Зареєструватися".
     * Збирає дані з урахуванням активних (видимих) полів введення, проводить повну
     * клієнтську валідацію на пусті значення та ідентичність паролів. При успішній перевірці
     * делегує створення акаунта сервісу та обробляє можливі виключення.
     */
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

    /**
     * Обробник події для повернення на екран авторизації.
     * Завантажує Login.fxml та адаптує розміри вікна під компактні габарити авторизаційного вікна.
     */
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