package com.noideasolutions.svitlo.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneSwitcher {

    /**
     * Універсальний метод для перемикання сцен.
     *
     * @param event    Подія натискання на кнопку (звідси беремо поточне вікно)
     * @param fxmlPath Шлях до FXML файлу (починаючи з /com/noideasolutions/...)
     * @param title    Заголовок нового вікна
     */
    public static void switchTo(ActionEvent event, String fxmlPath, String title) {
        try {
            // Шукаємо файл у папці resources
            URL resource = SceneSwitcher.class.getResource(fxmlPath);
            if (resource == null) {
                System.err.println(" Помилка: FXML файл не знайдено за шляхом: " + fxmlPath);
                return;
            }

            // Завантажуємо дизайн
            Parent root = FXMLLoader.load(resource);

            // Отримуємо поточне вікно (Stage)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Створюємо нову сцену (розмір підлаштується автоматично під FXML)
            Scene scene = new Scene(root);

            // Застосовуємо зміни
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            System.err.println(" Помилка завантаження сцени: " + fxmlPath);
            e.printStackTrace();
        }
    }
}