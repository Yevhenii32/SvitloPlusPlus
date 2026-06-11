package com.noideasolutions.svitlo.service;



import com.noideasolutions.svitlo.model.Hub;

public class SystemNotificationService {

    public void notifyHubFound(int guestId, Hub hub, double distanceKm) {
        System.out.println(
                "Сповіщення для гостя ID " + guestId +
                        ": знайдено онлайн-хаб '" + hub.getTitle() +
                        "' на відстані " + String.format("%.2f", distanceKm) + " км."
        );
    }

    public void notifyWaiting(int guestId) {
        System.out.println(
                "Сповіщення для гостя ID " + guestId +
                        ": у заданому радіусі поки немає онлайн-хабів. Радар продовжує пошук..."
        );
    }
}