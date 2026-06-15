package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.model.Hub;
import javafx.scene.control.Alert;

/**
 * Сервіс для генерації системних сповіщень та інтерактивних вікон у додатку.
 * Забезпечує виведення діалогових вікон JavaFX та дублювання важливих подій у консольний лог.
 */
public class SystemNotificationService {

    /**
     * Інформує гостя про успішне виявлення хабу поруч за допомогою спливаючого вікна JavaFX Alert.
     * Виводить детальну інформацію про назву хабу та розраховану відстань до нього.
     */
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

    /**
     * Логує стан режиму очікування, коли радар запущений, але активних локацій поруч не виявлено.
     * Сповіщення виводиться виключно в консоль для запобігання блокування інтерфейсу спамом вікон.
     */
    public void notifyWaiting(int guestId) {
        System.out.println(
                "Сповіщення для гостя ID " + guestId +
                        ": у заданому радіусі поки немає онлайн-хабів. Радар продовжує пошук..."
        );
    }
}