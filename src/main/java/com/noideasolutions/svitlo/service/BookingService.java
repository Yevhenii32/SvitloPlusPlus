package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.dao.BookingDAO;
import com.noideasolutions.svitlo.dao.HubDAO;
import com.noideasolutions.svitlo.model.Booking;
import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.exception.SvitloException;
import com.noideasolutions.svitlo.exception.NotEnoughSlotsException;
import com.noideasolutions.svitlo.exception.HubNotFoundException;

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

        if (userId <= 0) throw new IllegalArgumentException("ID користувача має бути додатним");
        if (hub.getId() <= 0) throw new IllegalArgumentException("ID хабу має бути додатним");
        if (requestedSlots <= 0) throw new IllegalArgumentException("Кількість місць для бронювання має бути більшою за нуль");

        // БІЗНЕС-ВИНЯТКИ
        if (!hub.isActive()) {
            throw new HubNotFoundException("Цей хаб наразі неактивний і недоступний для бронювання.");
        }
        if (hub.getSlotsAvailable() > hub.getSlotsTotal()) {
            throw new SvitloException("Критична помилка даних: вільних місць більше, ніж місткість хабу.");
        }

        if (requestedSlots > hub.getSlotsAvailable()) {
            throw new NotEnoughSlotsException("Недостатньо вільних місць. Доступно: " + hub.getSlotsAvailable() + ", запитано: " + requestedSlots);
        }

        Booking booking = new Booking(userId, hub.getId(), requestedSlots);
        boolean saved = bookingDAO.save(booking);

        if (!saved) {
            throw new SvitloException("Помилка сервера: не вдалося зберегти бронювання в базу даних.");
        }

        int oldSlots = hub.getSlotsAvailable();
        hub.setSlotsAvailable(oldSlots - requestedSlots);
        boolean updated = hubDAO.update(hub);

        if (!updated) {
            hub.setSlotsAvailable(oldSlots);
            // Якщо хаб не оновився, видаляємо створене бронювання, щоб не було сміття в БД
            bookingDAO.deleteById(booking.getId());
            throw new SvitloException("Помилка сервера: не вдалося оновити кількість місць у хабі.");
        }
    }

    @Override
    public void cancelBooking(Hub hub, int bookingId, int releasedSlots) {
        validateHub(hub);
        if (releasedSlots <= 0) throw new IllegalArgumentException("Кількість місць для скасування має бути додатньою");

        if (hub.getSlotsAvailable() + releasedSlots > hub.getSlotsTotal()) {
            throw new SvitloException("Помилка логіки: після скасування вільних місць стане більше, ніж місткість хабу.");
        }

        int oldSlots = hub.getSlotsAvailable();
        hub.setSlotsAvailable(oldSlots + releasedSlots);
        boolean updated = hubDAO.update(hub);

        if (!updated) {
            hub.setSlotsAvailable(oldSlots);
            throw new SvitloException("Помилка сервера: не вдалося повернути місця хабу в базі даних.");
        }

        // Видаляємо саме бронювання з бази даних
        boolean deleted = bookingDAO.deleteById(bookingId);
        if (!deleted) {
            System.err.println("Помилка: не вдалося видалити запис бронювання з БД!");
        }
    }

    private void validateHub(Hub hub) {
        if (hub == null) {
            throw new HubNotFoundException("Хаб не знайдено.");
        }

        if (hub.getSlotsTotal() < 0) {
            throw new SvitloException("Помилка даних хабу: загальна кількість місць не може бути від'ємною");
        }

        if (hub.getSlotsAvailable() < 0) {
            throw new SvitloException("Помилка даних хабу: кількість вільних місць не може бути від'ємною");
        }
    }
}