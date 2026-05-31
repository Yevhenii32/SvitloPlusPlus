package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.model.Hub;

public interface IBookingService {
    boolean canBook(Hub hub,int requestedSlots);
    void bookSlots(Hub hub, int userId, int requestedSlots);
    void cancelBooking(Hub hub, int releasedSlots);

}
