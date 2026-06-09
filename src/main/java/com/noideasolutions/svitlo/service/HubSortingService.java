package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.model.Hub;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HubSortingService {

    public List<Hub> getSortedHubs(double userLat, double userLon, List<Hub> allHubs) {
        if (allHubs == null) {
            return new ArrayList<>();
        }

        List<Hub> sortedHubs = new ArrayList<>(allHubs);

        sortedHubs.sort(
                Comparator
                        .<Hub>comparingDouble(hub -> calculateDistanceKm(
                                userLat,
                                userLon,
                                hub.getLatitude(),
                                hub.getLongitude()
                        ))
                        .thenComparing(
                                Comparator.comparingInt(Hub::getSlotsAvailable).reversed()
                        )
        );

        return sortedHubs;
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
    public List<Hub> getSortedHubsBySlots(double userLat, double userLon, List<Hub> allHubs) {
        if (allHubs == null) {
            return new ArrayList<>();
        }

        List<Hub> sortedHubs = new ArrayList<>(allHubs);

        sortedHubs.sort(
                Comparator
                        .comparingInt(Hub::getSlotsAvailable)
                        .reversed()
                        .thenComparingDouble(hub -> calculateDistanceKm(
                                userLat,
                                userLon,
                                hub.getLatitude(),
                                hub.getLongitude()
                        ))
        );

        return sortedHubs;
    }
}