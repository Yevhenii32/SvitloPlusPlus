package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.model.Hub;

public class BookingService implements  IBookingService{
@Override
    public boolean canBook(Hub hub, int requestedSlots) {
        if (hub == null) {
            return false;
        }

        if (!hub.isActive()) {
            return false;
        }

        if (requestedSlots <= 0) {
            return false;
        }

        if (hub.getSlotsTotal() < 0 || hub.getSlotsAvailable() < 0) {
            return false;
        }

        if (hub.getSlotsAvailable() > hub.getSlotsTotal()) {
            return false;
        }

        return requestedSlots <= hub.getSlotsAvailable();
    }
@Override
    public void bookSlots(Hub hub, int requestedSlots) {
        validateHub(hub);

        if (requestedSlots <= 0) {
            throw new IllegalArgumentException("Requested slots must be positive");
        }

        if (!hub.isActive()) {
            throw new IllegalStateException("Hub is not active");
        }

        if (hub.getSlotsAvailable() > hub.getSlotsTotal()) {
            throw new IllegalStateException("Available slots cannot be greater than total slots");
        }

        if (requestedSlots > hub.getSlotsAvailable()) {
            throw new IllegalStateException("Not enough available slots");
        }

        hub.setSlotsAvailable(hub.getSlotsAvailable() - requestedSlots);
    }

    public void cancelBooking(Hub hub, int releasedSlots) {
        validateHub(hub);

        if (releasedSlots <= 0) {
            throw new IllegalArgumentException("Released slots must be positive");
        }

        if (hub.getSlotsAvailable() + releasedSlots > hub.getSlotsTotal()) {
            throw new IllegalStateException("Available slots cannot be greater than total slots");
        }

        hub.setSlotsAvailable(hub.getSlotsAvailable() + releasedSlots);
    }

    private void validateHub(Hub hub) {
        if (hub == null) {
            throw new IllegalArgumentException("Hub cannot be null");
        }

        if (hub.getSlotsTotal() <= 0) {
            throw new IllegalStateException("Total slots cannot be empty ");
        }

        if (hub.getSlotsAvailable() < 0) {
            throw new IllegalStateException("Available slots cannot be negative");
        }
    }
}