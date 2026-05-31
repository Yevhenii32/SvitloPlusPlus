package com.noideasolutions.svitlo.service;


import com.noideasolutions.svitlo.dao.HubDAO;
import com.noideasolutions.svitlo.model.Hub;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RadarService {

    private final HubDAO hubDAO;
    private Timer timer;
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
        timer = new Timer(true);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkNearbyHubs(guestLatitude, guestLongitude, radiusKm);
            }
        }, 0, 60_000);
    }

    public void stopSearch() {
        if (timer != null) {
            timer.cancel();
            timer = null;
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
            System.out.println("У заданому радіусі поки немає доступних хабів. Радар продовжує пошук...");
            waitingMessageShown = true;
        }
    }

    private void sendNotification(Hub hub, double distanceKm) {
        System.out.println(
                "Знайдено хаб поруч: " + hub.getTitle()
                        + ", відстань: " + String.format("%.2f", distanceKm) + " км"
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