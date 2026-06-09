package com.noideasolutions.svitlo.service;



import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.model.PartnerReward;
import com.noideasolutions.svitlo.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BonusServiceTest {

    @Test
    void awardPointsForBookingShouldDoNothingWhenHubIsNull() {
        BonusService service = new BonusService();

        assertDoesNotThrow(() ->
                service.awardPointsForBooking(null, 5)
        );
    }

    @Test
    void awardPointsForBookingShouldDoNothingWhenBookedSlotsIsZero() {
        BonusService service = new BonusService();

        Hub hub = new Hub(1, "Test Hub", "Internet", 50.45, 30.52, 5);

        assertDoesNotThrow(() ->
                service.awardPointsForBooking(hub, 0)
        );
    }

    @Test
    void awardPointsForBookingShouldDoNothingWhenBookedSlotsIsNegative() {
        BonusService service = new BonusService();

        Hub hub = new Hub(1, "Test Hub", "Internet", 50.45, 30.52, 5);

        assertDoesNotThrow(() ->
                service.awardPointsForBooking(hub, -5)
        );
    }

    @Test
    void redeemRewardShouldReturnFalseWhenUserIsNull() {
        BonusService service = new BonusService();

        PartnerReward reward =
                new PartnerReward(1, "Silpo", "Discount", "10% off", 100);

        assertFalse(
                service.redeemReward(null, reward)
        );
    }

    @Test
    void redeemRewardShouldReturnFalseWhenRewardIsNull() {
        BonusService service = new BonusService();

        User user = new User();
        user.setBonusPoints(200);

        assertFalse(
                service.redeemReward(user, null)
        );
    }

    @Test
    void redeemRewardShouldReturnFalseWhenUserHasNotEnoughPoints() {
        BonusService service = new BonusService();

        User user = new User();
        user.setUsername("Nikita");
        user.setBonusPoints(50);

        PartnerReward reward =
                new PartnerReward(1, "Silpo", "Discount", "10% off", 100);

        boolean result = service.redeemReward(user, reward);

        assertFalse(result);
        assertEquals(50, user.getBonusPoints());
    }
}