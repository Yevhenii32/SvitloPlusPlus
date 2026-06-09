package com.noideasolutions.svitlo.service;



import com.noideasolutions.svitlo.model.Hub;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportServiceTest {

    @Test
    void submitReportShouldThrowWhenHubIsNull() {
        ReportService service = new ReportService();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.submitReport(1, null, "Bad hub")
        );
    }

    @Test
    void submitReportShouldThrowWhenReasonIsNull() {
        ReportService service = new ReportService();

        Hub hub = new Hub(1, "Test Hub", "Internet", 50.45, 30.52, 5);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.submitReport(1, hub, null)
        );
    }

    @Test
    void submitReportShouldThrowWhenReasonIsBlank() {
        ReportService service = new ReportService();

        Hub hub = new Hub(1, "Test Hub", "Internet", 50.45, 30.52, 5);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.submitReport(1, hub, "   ")
        );
    }

    @Test
    void hubShouldBeBlockedAfterThirdReportLogic() {
        Hub hub = new Hub(1, "Test Hub", "Internet", 50.45, 30.52, 5);
        hub.setReportCount(2);
        hub.setActive(true);

        int currentCount = hub.getReportCount();
        hub.setReportCount(currentCount + 1);

        if (hub.getReportCount() >= 3) {
            hub.setActive(false);
        }

        assertEquals(3, hub.getReportCount());
        assertFalse(hub.isActive());
    }

    @Test
    void hubShouldStayActiveBeforeThirdReportLogic() {
        Hub hub = new Hub(1, "Test Hub", "Internet", 50.45, 30.52, 5);
        hub.setReportCount(1);
        hub.setActive(true);

        int currentCount = hub.getReportCount();
        hub.setReportCount(currentCount + 1);

        if (hub.getReportCount() >= 3) {
            hub.setActive(false);
        }

        assertEquals(2, hub.getReportCount());
        assertTrue(hub.isActive());
    }
}