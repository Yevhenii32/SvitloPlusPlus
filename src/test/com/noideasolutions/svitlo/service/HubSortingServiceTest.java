package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.model.Hub;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HubSortingServiceTest {

    @Test
    void getSortedHubsShouldSortByDistanceFirst() {
        Hub nearHub = new Hub(1, "Near Hub", "Near", 50.4501, 30.5234, 2);
        Hub farHub = new Hub(1, "Far Hub", "Far", 50.5000, 30.6000, 10);

        HubSortingService service = new HubSortingService();

        List<Hub> result = service.getSortedHubs(
                50.4500,
                30.5230,
                List.of(farHub, nearHub)
        );

        assertEquals("Near Hub", result.get(0).getTitle());
        assertEquals("Far Hub", result.get(1).getTitle());
    }

    @Test
    void getSortedHubsShouldSortByAvailableSlotsWhenDistanceIsSame() {
        Hub smallHub = new Hub(1, "Small Hub", "Same distance", 50.4501, 30.5234, 2);
        smallHub.setSlotsAvailable(2);

        Hub bigHub = new Hub(1, "Big Hub", "Same distance", 50.4501, 30.5234, 10);
        bigHub.setSlotsAvailable(10);

        HubSortingService service = new HubSortingService();

        List<Hub> result = service.getSortedHubs(
                50.4500,
                30.5230,
                List.of(smallHub, bigHub)
        );

        assertEquals("Big Hub", result.get(0).getTitle());
        assertEquals("Small Hub", result.get(1).getTitle());
    }

    @Test
    void getSortedHubsShouldReturnEmptyListWhenInputIsNull() {
        HubSortingService service = new HubSortingService();

        List<Hub> result = service.getSortedHubs(50.4500, 30.5230, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getSortedHubsShouldNotChangeOriginalList() {
        Hub nearHub = new Hub(1, "Near Hub", "Near", 50.4501, 30.5234, 2);
        Hub farHub = new Hub(1, "Far Hub", "Far", 50.5000, 30.6000, 10);

        List<Hub> original = List.of(farHub, nearHub);

        HubSortingService service = new HubSortingService();
        List<Hub> result = service.getSortedHubs(50.4500, 30.5230, original);

        assertEquals("Far Hub", original.get(0).getTitle());
        assertEquals("Near Hub", original.get(1).getTitle());

        assertEquals("Near Hub", result.get(0).getTitle());
        assertEquals("Far Hub", result.get(1).getTitle());
    }
    @Test
    void getSortedHubsBySlotsShouldSortByAvailableSlotsFirstThenDistance() {
        Hub nearSmallHub = new Hub(1, "Near Small Hub", "Near but few slots", 50.4501, 30.5234, 2);
        nearSmallHub.setSlotsAvailable(2);

        Hub farBigHub = new Hub(1, "Far Big Hub", "Far but many slots", 50.5000, 30.6000, 10);
        farBigHub.setSlotsAvailable(10);

        Hub middleHub = new Hub(1, "Middle Hub", "Middle", 50.4600, 30.5300, 5);
        middleHub.setSlotsAvailable(5);

        HubSortingService service = new HubSortingService();

        List<Hub> result = service.getSortedHubsBySlots(
                50.4500,
                30.5230,
                List.of(nearSmallHub, farBigHub, middleHub)
        );

        assertEquals("Far Big Hub", result.get(0).getTitle());
        assertEquals("Middle Hub", result.get(1).getTitle());
        assertEquals("Near Small Hub", result.get(2).getTitle());
    }
    @Test
    void getSortedHubsBySlotsShouldSortByDistanceWhenSlotsAreSame() {
        Hub nearHub = new Hub(1, "Near Hub", "Near", 50.4501, 30.5234, 5);
        nearHub.setSlotsAvailable(5);

        Hub farHub = new Hub(1, "Far Hub", "Far", 50.5000, 30.6000, 5);
        farHub.setSlotsAvailable(5);

        HubSortingService service = new HubSortingService();

        List<Hub> result = service.getSortedHubsBySlots(
                50.4500,
                30.5230,
                List.of(farHub, nearHub)
        );

        assertEquals("Near Hub", result.get(0).getTitle());
        assertEquals("Far Hub", result.get(1).getTitle());
    }
}