package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.dao.BookingDAO;
import com.noideasolutions.svitlo.dao.HubDAO;
import com.noideasolutions.svitlo.model.Booking;
import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.exception.HubNotFoundException;
import com.noideasolutions.svitlo.exception.NotEnoughSlotsException;
import com.noideasolutions.svitlo.exception.SvitloException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

    private static class FakeHubDAO extends HubDAO {
        @Override
        public boolean update(Hub hub) {
            return true;
        }
    }

    private static class FakeBookingDAO extends BookingDAO {
        @Override
        public boolean save(Booking booking) {
            booking.setId(999);
            return true;
        }
        @Override
        public boolean deleteById(int id) {
            return true;
        }
    }

    private BookingService createService() {
        return new BookingService(new FakeHubDAO(), new FakeBookingDAO());
    }

    @Test
    void canBookReturnsTrueWhenEnoughSlots() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        BookingService service = createService();
        assertTrue(service.canBook(hub, 3));
    }

    @Test
    void canBookReturnsFalseWhenHubIsNull() {
        BookingService service = createService();
        assertFalse(service.canBook(null, 1));
    }

    @Test
    void canBookReturnsFalseWhenRequestedSlotsIsZero() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        BookingService service = createService();
        assertFalse(service.canBook(hub, 0));
    }

    @Test
    void canBookReturnsFalseWhenRequestedSlotsIsNegative() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        BookingService service = createService();
        assertFalse(service.canBook(hub, -2));
    }

    @Test
    void canBookReturnsFalseWhenNotEnoughSlots() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setSlotsAvailable(2);
        BookingService service = createService();
        assertFalse(service.canBook(hub, 3));
    }

    @Test
    void canBookReturnsFalseWhenHubIsInactive() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setActive(false);
        BookingService service = createService();
        assertFalse(service.canBook(hub, 1));
    }

    @Test
    void canBookReturnsFalseWhenAvailableSlotsGreaterThanTotalSlots() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setSlotsAvailable(6);
        BookingService service = createService();
        assertFalse(service.canBook(hub, 1));
    }

    @Test
    void bookSlotsDecreasesAvailableSlots() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setId(1);
        BookingService service = createService();
        service.bookSlots(hub, 1, 2);
        assertEquals(3, hub.getSlotsAvailable());
    }

    @Test
    void bookSlotsAllowsBookingAllAvailableSlots() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setId(1);
        BookingService service = createService();
        service.bookSlots(hub, 1, 5);
        assertEquals(0, hub.getSlotsAvailable());
    }

    // --- ОНОВЛЕНІ ТЕСТИ З НОВИМИ ВИНЯТКАМИ ---

    @Test
    void bookSlotsThrowsWhenHubIsNull() {
        BookingService service = createService();
        assertThrows(HubNotFoundException.class, () -> service.bookSlots(null, 1, 1));
    }

    @Test
    void bookSlotsThrowsWhenUserIdIsInvalid() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setId(1);
        BookingService service = createService();
        assertThrows(IllegalArgumentException.class, () -> service.bookSlots(hub, 0, 1));
    }

    @Test
    void bookSlotsThrowsWhenHubIdIsInvalid() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        BookingService service = createService();
        assertThrows(IllegalArgumentException.class, () -> service.bookSlots(hub, 1, 1));
    }

    @Test
    void bookSlotsThrowsWhenRequestedSlotsIsZero() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setId(1);
        BookingService service = createService();
        assertThrows(IllegalArgumentException.class, () -> service.bookSlots(hub, 1, 0));
    }

    @Test
    void bookSlotsThrowsWhenRequestedSlotsIsNegative() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setId(1);
        BookingService service = createService();
        assertThrows(IllegalArgumentException.class, () -> service.bookSlots(hub, 1, -1));
    }

    @Test
    void bookSlotsThrowsWhenHubIsInactive() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setId(1);
        hub.setActive(false);
        BookingService service = createService();
        assertThrows(HubNotFoundException.class, () -> service.bookSlots(hub, 1, 1));
    }

    @Test
    void bookSlotsThrowsWhenNotEnoughSlots() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setId(1);
        hub.setSlotsAvailable(2);
        BookingService service = createService();
        assertThrows(NotEnoughSlotsException.class, () -> service.bookSlots(hub, 1, 3));
        assertEquals(2, hub.getSlotsAvailable());
    }

    @Test
    void bookSlotsThrowsWhenTotalSlotsIsNegative() {
        Hub hub = new Hub();
        hub.setId(1);
        hub.setSlotsTotal(-1);
        hub.setSlotsAvailable(0);
        BookingService service = createService();
        assertThrows(SvitloException.class, () -> service.bookSlots(hub, 1, 1));
    }

    @Test
    void bookSlotsThrowsWhenAvailableSlotsIsNegative() {
        Hub hub = new Hub();
        hub.setId(1);
        hub.setSlotsTotal(5);
        hub.setSlotsAvailable(-1);
        BookingService service = createService();
        assertThrows(SvitloException.class, () -> service.bookSlots(hub, 1, 1));
    }

    @Test
    void bookSlotsThrowsWhenAvailableSlotsGreaterThanTotalSlots() {
        Hub hub = new Hub();
        hub.setId(1);
        hub.setSlotsTotal(5);
        hub.setSlotsAvailable(6);
        hub.setActive(true);
        BookingService service = createService();
        assertThrows(SvitloException.class, () -> service.bookSlots(hub, 1, 1));
    }

    @Test
    void cancelBookingIncreasesAvailableSlots() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setSlotsAvailable(2);
        BookingService service = createService();
        service.cancelBooking(hub, 999, 2);
        assertEquals(4, hub.getSlotsAvailable());
    }

    @Test
    void cancelBookingThrowsWhenReleasedSlotsIsZero() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        BookingService service = createService();
        assertThrows(IllegalArgumentException.class, () -> service.cancelBooking(hub, 999, 0));
    }

    @Test
    void cancelBookingThrowsWhenReleasedSlotsIsNegative() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        BookingService service = createService();
        assertThrows(IllegalArgumentException.class, () -> service.cancelBooking(hub, 999, -1));
    }

    @Test
    void cancelBookingThrowsWhenSlotsWouldBecomeGreaterThanTotal() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setSlotsAvailable(4);
        BookingService service = createService();
        assertThrows(SvitloException.class, () -> service.cancelBooking(hub, 999, 2));
        assertEquals(4, hub.getSlotsAvailable());
    }
}