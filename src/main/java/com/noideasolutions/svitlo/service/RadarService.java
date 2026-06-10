package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.dao.HubDAO;
import com.noideasolutions.svitlo.model.Hub;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RadarService {

    private final HubDAO hubDAO;
    private ScheduledExecutorService executor;
    private boolean waitingMessageShown = false;

    public RadarService() {
        this.hubDAO = new HubDAO();
    }

    public RadarService(HubDAO hubDAO) {
        this.hubDAO = hubDAO;
    }

    public void startSearch(double guestLatitude, double guestLongitude, double radiusKm) {
        stopSearch();

        waitingMessageShown = false;

        executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {
            try {
                checkNearbyHubs(guestLatitude, guestLongitude, radiusKm);
            } catch (Exception e) {
                Platform.runLater(() ->
                        System.err.println("Помилка в RadarService: " + e.getMessage())
                );
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    public void stopSearch() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
            executor = null;
        }
    }

    private void checkNearbyHubs(double guestLatitude, double guestLongitude, double radiusKm) {
        List<Hub> activeHubs = hubDAO.findAllActive();

        if (activeHubs.isEmpty()) {
            showWaitingMessage();
            return;
        }

        boolean found = false;

        for (Hub hub : activeHubs) {
            double distance = calculateDistanceKm(
                    guestLatitude,
                    guestLongitude,
                    hub.getLatitude(),
                    hub.getLongitude()
            );

            if (distance <= radiusKm && hub.getSlotsAvailable() > 0) {
                found = true;
                waitingMessageShown = false;
                sendNotification(hub, distance);
            }
        }

        if (!found) {
            showWaitingMessage();
        }
    }

    private void showWaitingMessage() {
        if (!waitingMessageShown) {
            Platform.runLater(() ->
                    System.out.println("У заданому радіусі поки немає доступних хабів. Радар продовжує пошук...")
            );
            waitingMessageShown = true;
        }
    }

    private void sendNotification(Hub hub, double distanceKm) {
        Platform.runLater(() ->
                System.out.println(
                        "Знайдено хаб поруч: " + hub.getTitle()
                                + ", відстань: " + String.format("%.2f", distanceKm) + " км"
                )
        );
    }

    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int earthRadiusKm = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }
}