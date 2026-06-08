package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.model.PartnerReward;
import com.noideasolutions.svitlo.model.User;

public class BonusService {

    // Коефіцієнт: за кожен заброньований слот Хост отримує 10 балів
    private static final int POINTS_PER_SLOT = 10;

    /**
     * Нараховує бали Хосту, коли Гість успішно забронював у нього місця.
     */
    public void awardPointsForBooking(Hub hub, int bookedSlots) {
        if (hub == null || bookedSlots <= 0) return;

        int pointsToAward = bookedSlots * POINTS_PER_SLOT;
        int hostId = hub.getOwnerId(); // Припускаємо, що у Hub є ID власника

        System.out.println("LOG: Хосту з ID " + hostId + " нараховано " + pointsToAward + " балів за " + bookedSlots + " слотів.");

        // TODO: Коли Вася оновить UserDAO, тут буде виклик:
        // userDAO.updateBonusPoints(hostId, pointsToAward);
    }

    /**
     * Логіка обміну балів користувача на знижку партнера.
     * @return true, якщо обмін успішний
     */
    public boolean redeemReward(User user, PartnerReward reward) {
        if (user == null || reward == null) return false;

        // Перевіряємо, чи вистачає балів (поки що виведемо в консоль для тесту)
        // int currentPoints = user.getBonusPoints();
        int currentPoints = 100; // Тимчасова заглушка для тестів

        if (currentPoints < reward.getCostInPoints()) {
            System.out.println("LOG: Недостатньо балів для користувача " + user.getUsername());
            return false;
        }

        int remainingPoints = currentPoints - reward.getCostInPoints();
        System.out.println("LOG: Користувач " + user.getUsername() + " успішно придбав '"
                + reward.getTitle() + "' від " + reward.getPartnerName() + ". Залишок балів: " + remainingPoints);

        // TODO: Оновити дані в базі через DAO
        // userDAO.setBonusPoints(user.getId(), remainingPoints);

        return true;
    }
}