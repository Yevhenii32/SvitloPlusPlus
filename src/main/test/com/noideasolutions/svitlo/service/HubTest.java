package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.model.Hub;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HubTest {
    @Test
    void constructorShouldSetDefSlotsAndActiveStatus() {
        Hub hub = new Hub(1, "Test Hub", "Internet", 50.45, 30.52, 5);
        assertEquals(5, hub.getSlotsAvailable());
        assertEquals(5, hub.getSlotsTotal());
        assertTrue(hub.isActive());
    }

}
