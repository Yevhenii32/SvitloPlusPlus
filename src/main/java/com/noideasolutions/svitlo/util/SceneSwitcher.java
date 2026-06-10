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

    public static void switchTo(ActionEvent event, String fxmlPath, String title) {
        try {
            URL resource = SceneSwitcher.class.getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Помилка: FXML файл не знайдено за шляхом: " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            System.err.println("Помилка завантаження сцени: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static <T> T switchToWithController(ActionEvent event, String fxmlPath, String title) {
        try {
            URL resource = SceneSwitcher.class.getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Помилка: FXML файл не знайдено за шляхом: " + fxmlPath);
                return null;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

            return loader.getController();

        } catch (IOException e) {
            System.err.println("Помилка завантаження сцени: " + fxmlPath);
            e.printStackTrace();
            return null;
        }
    }
}