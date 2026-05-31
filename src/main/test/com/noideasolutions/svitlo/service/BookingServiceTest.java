package com.noideasolutions.svitlo.service;
import com.noideasolutions.svitlo.model.Hub;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

    @Test
    void canBookReturnsTrueWhenEnoughSlots() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        BookingService service = new BookingService();

        assertTrue(service.canBook(hub, 3));
    }

    @Test
    void canBookReturnsFalseWhenHubIsNull() {
        BookingService service = new BookingService();

        assertFalse(service.canBook(null, 1));
    }

    @Test
    void canBookReturnsFalseWhenRequestedSlotsIsZero() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        BookingService service = new BookingService();

        assertFalse(service.canBook(hub, 0));
    }

    @Test
    void canBookReturnsFalseWhenRequestedSlotsIsNegative() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        BookingService service = new BookingService();

        assertFalse(service.canBook(hub, -2));
    }

    @Test
    void canBookReturnsFalseWhenNotEnoughSlots() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setSlotsAvailable(2);

        BookingService service = new BookingService();

        assertFalse(service.canBook(hub, 3));
    }

    @Test
    void canBookReturnsFalseWhenHubIsInactive() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setActive(false);

        BookingService service = new BookingService();

        assertFalse(service.canBook(hub, 1));
    }

    @Test
    void canBookReturnsFalseWhenAvailableSlotsGreaterThanTotalSlots() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setSlotsAvailable(6);

        BookingService service = new BookingService();

        assertFalse(service.canBook(hub, 1));
    }

    @Test
    void bookSlotsDecreasesAvailableSlots() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        BookingService service = new BookingService();

        service.bookSlots(hub, 2);

        assertEquals(3, hub.getSlotsAvailable());
    }

    @Test
    void bookSlotsAllowsBookingAllAvailableSlots() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        BookingService service = new BookingService();

        service.bookSlots(hub, 5);

        assertEquals(0, hub.getSlotsAvailable());
    }

    @Test
    void bookSlotsThrowsWhenHubIsNull() {
        BookingService service = new BookingService();

        assertThrows(IllegalArgumentException.class, () -> service.bookSlots(null, 1));
    }

    @Test
    void bookSlotsThrowsWhenRequestedSlotsIsZero() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        BookingService service = new BookingService();

        assertThrows(IllegalArgumentException.class, () -> service.bookSlots(hub, 0));
    }

    @Test
    void bookSlotsThrowsWhenRequestedSlotsIsNegative() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        BookingService service = new BookingService();

        assertThrows(IllegalArgumentException.class, () -> service.bookSlots(hub, -1));
    }

    @Test
    void bookSlotsThrowsWhenHubIsInactive() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setActive(false);

        BookingService service = new BookingService();

        assertThrows(IllegalStateException.class, () -> service.bookSlots(hub, 1));
    }

    @Test
    void bookSlotsThrowsWhenNotEnoughSlots() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setSlotsAvailable(2);

        BookingService service = new BookingService();

        assertThrows(IllegalStateException.class, () -> service.bookSlots(hub, 3));
        assertEquals(2, hub.getSlotsAvailable());
    }

    @Test
    void bookSlotsThrowsWhenTotalSlotsIsNegative() {
        Hub hub = new Hub();
        hub.setSlotsTotal(-1);
        hub.setSlotsAvailable(0);

        BookingService service = new BookingService();

        assertThrows(IllegalStateException.class, () -> service.bookSlots(hub, 1));
    }

    @Test
    void bookSlotsThrowsWhenAvailableSlotsIsNegative() {
        Hub hub = new Hub();
        hub.setSlotsTotal(5);
        hub.setSlotsAvailable(-1);

        BookingService service = new BookingService();

        assertThrows(IllegalStateException.class, () -> service.bookSlots(hub, 1));
    }

    @Test
    void bookSlotsThrowsWhenAvailableSlotsGreaterThanTotalSlots() {
        Hub hub = new Hub();
        hub.setSlotsTotal(5);
        hub.setSlotsAvailable(6);
        hub.setActive(true);

        BookingService service = new BookingService();

        assertThrows(IllegalStateException.class, () -> service.bookSlots(hub, 1));
    }

    @Test
    void cancelBookingIncreasesAvailableSlots() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setSlotsAvailable(2);

        BookingService service = new BookingService();

        service.cancelBooking(hub, 2);

        assertEquals(4, hub.getSlotsAvailable());
    }

    @Test
    void cancelBookingThrowsWhenReleasedSlotsIsZero() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        BookingService service = new BookingService();

        assertThrows(IllegalArgumentException.class, () -> service.cancelBooking(hub, 0));
    }

    @Test
    void cancelBookingThrowsWhenReleasedSlotsIsNegative() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        BookingService service = new BookingService();

        assertThrows(IllegalArgumentException.class, () -> service.cancelBooking(hub, -1));
    }

    @Test
    void cancelBookingThrowsWhenSlotsWouldBecomeGreaterThanTotal() {
        Hub hub = new Hub(1, "Test Hub", "Internet and power", 50.45, 30.52, 5);
        hub.setSlotsAvailable(4);

        BookingService service = new BookingService();

        assertThrows(IllegalStateException.class, () -> service.cancelBooking(hub, 2));
        assertEquals(4, hub.getSlotsAvailable());
    }
}