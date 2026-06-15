package com.noideasolutions.svitlo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Завантаження FXML файлу з папки resources
        Parent root = FXMLLoader.load(getClass().getResource("/com/noideasolutions/svitlo/controller/Login.fxml"));

        // Встановлюємо розміри вікна
        Scene scene = new Scene(root, 520, 650);

        primaryStage.setTitle("Svitlo++ - Авторизація");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/noideasolutions/svitlo/images/logo.png")));

        primaryStage.show();



    }

    public static void main(String[] args) {
        launch(args);
    }
}