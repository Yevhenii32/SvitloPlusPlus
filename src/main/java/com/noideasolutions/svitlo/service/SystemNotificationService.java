package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.model.Hub;
import javafx.scene.control.Alert;

public class SystemNotificationService {

    public void notifyHubFound(int guestId, Hub hub, double distanceKm) {
        System.out.println(
                "Сповіщення для гостя ID " + guestId +
                        ": знайдено онлайн-хаб '" + hub.getTitle() +
                        "' на відстані " + String.format("%.2f", distanceKm) + " км."
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Радар знайшов хаб");
        alert.setHeaderText("Знайдено відповідний хаб");
        alert.setContentText(
                "Хаб: " + hub.getTitle() +
                        "\nВідстань: " + String.format("%.2f", distanceKm) + " км."
        );
        alert.show();
    }

    public void notifyWaiting(int guestId) {
        System.out.println(
                "Сповіщення для гостя ID " + guestId +
                        ": у заданому радіусі поки немає онлайн-хабів. Радар продовжує пошук..."
        );
    }
}