package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.dao.HubDAO;
import com.noideasolutions.svitlo.model.Hub;
import javafx.application.Platform;
import com.noideasolutions.svitlo.model.RadarRequest;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RadarService {
    private final SystemNotificationService notificationService;
    private final HubDAO hubDAO;
    private ScheduledExecutorService executor;
    private boolean waitingMessageShown = false;

    public RadarService() {
        this.hubDAO = new HubDAO();
        this.notificationService = new SystemNotificationService();
    }

    public RadarService(HubDAO hubDAO) {
        this.hubDAO = hubDAO;
        this.notificationService = new SystemNotificationService();
    }

    public void startSearch(int guestId, double guestLatitude, double guestLongitude, double radiusKm) {
        stopSearch();

        waitingMessageShown = false;

        executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {
            try {
                checkNearbyHubs(guestId, guestLatitude, guestLongitude, radiusKm);
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

    private void checkNearbyHubs(int guestId, double guestLatitude, double guestLongitude, double radiusKm) {
        List<Hub> activeHubs = hubDAO.findAllActive();

        if (activeHubs.isEmpty()) {
            showWaitingMessage(guestId);
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

            if (distance <= radiusKm && hub.getSlotsAvailable() > 0 && hub.isActive()) {
                found = true;
                waitingMessageShown = false;
                sendNotification(guestId, hub, distance);
            }
        }

        if (!found) {
            showWaitingMessage(guestId);
        }
    }
    private void showWaitingMessage(int guestId) {
        if (!waitingMessageShown) {
            Platform.runLater(() ->
                    notificationService.notifyWaiting(guestId)
            );
            waitingMessageShown = true;
        }
    }

    private void sendNotification(int guestId, Hub hub, double distanceKm) {
        Platform.runLater(() ->
                notificationService.notifyHubFound(guestId, hub, distanceKm)
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
    public RadarRequest createSearchRequest(int guestId, double guestLatitude, double guestLongitude) {
        if (guestId <= 0) {
            throw new IllegalArgumentException("Guest ID must be positive");
        }

        double defaultRadiusKm = 1.0;

        return new RadarRequest(
                guestId,
                guestLatitude,
                guestLongitude,
                defaultRadiusKm
        );
    }
}