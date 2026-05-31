package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.dao.BookingDAO;
import com.noideasolutions.svitlo.dao.HubDAO;
import com.noideasolutions.svitlo.model.Booking;
import com.noideasolutions.svitlo.model.Hub;

public class BookingService implements IBookingService {

    private final HubDAO hubDAO;
    private final BookingDAO bookingDAO;

    public BookingService() {
        this.hubDAO = new HubDAO();
        this.bookingDAO = new BookingDAO();
    }

    public BookingService(HubDAO hubDAO, BookingDAO bookingDAO) {
        this.hubDAO = hubDAO;
        this.bookingDAO = bookingDAO;
    }

    @Override
    public boolean canBook(Hub hub, int requestedSlots) {
        if (hub == null) return false;
        if (!hub.isActive()) return false;
        if (requestedSlots <= 0) return false;
        if (hub.getSlotsTotal() < 0 || hub.getSlotsAvailable() < 0) return false;
        if (hub.getSlotsAvailable() > hub.getSlotsTotal()) return false;

        return requestedSlots <= hub.getSlotsAvailable();
    }



    @Override
    public void bookSlots(Hub hub, int userId, int requestedSlots) {
        validateHub(hub);
        if (userId <= 0) throw new IllegalArgumentException("User ID must be positive");
        if (hub.getId() <= 0) throw new IllegalArgumentException("Hub ID must be positive");
        if (requestedSlots <= 0) throw new IllegalArgumentException("Requested slots must be positive");
        if (!hub.isActive()) throw new IllegalStateException("Hub is not active");
        if (hub.getSlotsAvailable() > hub.getSlotsTotal()) throw new IllegalStateException("Available slots cannot be greater than total slots");
        if (requestedSlots > hub.getSlotsAvailable()) throw new IllegalStateException("Not enough available slots");

        Booking booking = new Booking(userId, hub.getId(), requestedSlots);
        boolean saved = bookingDAO.save(booking);

        if (!saved) {
            throw new IllegalStateException("Failed to save booking in database");
        }

        int oldSlots = hub.getSlotsAvailable();
        hub.setSlotsAvailable(oldSlots - requestedSlots);
        boolean updated = hubDAO.update(hub);

        if (!updated) {
            hub.setSlotsAvailable(oldSlots);
            // Якщо хаб не оновився, видаляємо створене бронювання, щоб не було сміття в БД
            bookingDAO.deleteById(booking.getId());
            throw new IllegalStateException("Failed to update hub slots in database");
        }
    }

    @Override
    public void cancelBooking(Hub hub, int bookingId, int releasedSlots) {
        validateHub(hub);
        if (releasedSlots <= 0) throw new IllegalArgumentException("Released slots must be positive");
        if (hub.getSlotsAvailable() + releasedSlots > hub.getSlotsTotal()) {
            throw new IllegalStateException("Available slots cannot be greater than total slots");
        }

        int oldSlots = hub.getSlotsAvailable();
        hub.setSlotsAvailable(oldSlots + releasedSlots);
        boolean updated = hubDAO.update(hub);

        if (!updated) {
            hub.setSlotsAvailable(oldSlots);
            throw new IllegalStateException("Failed to update hub slots in database");
        }

        // Видаляємо саме бронювання з бази даних
        boolean deleted = bookingDAO.deleteById(bookingId);
        if (!deleted) {
            System.err.println("Помилка: не вдалося видалити запис бронювання з БД!");
        }
    }

    private void validateHub(Hub hub) {
        if (hub == null) {
            throw new IllegalArgumentException("Hub cannot be null");
        }

        if (hub.getSlotsTotal() < 0) {
            throw new IllegalStateException("Total slots cannot be negative");
        }

        if (hub.getSlotsAvailable() < 0) {
            throw new IllegalStateException("Available slots cannot be negative");
        }
    }
}