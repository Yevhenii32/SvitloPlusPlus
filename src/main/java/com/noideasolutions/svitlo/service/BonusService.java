package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.dao.UserDAO;
import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.model.PartnerReward;
import com.noideasolutions.svitlo.model.User;
import com.noideasolutions.svitlo.exception.SvitloException;

public class BonusService {

    private final UserDAO userDAO;
    private static final int POINTS_PER_SLOT = 10;

    public BonusService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Нараховує бали Хосту, коли Гість успішно забронював у нього місця.
     */
    public void awardPointsForBooking(Hub hub, int bookedSlots) {
        if (hub == null || bookedSlots <= 0) return;

        int pointsToAward = bookedSlots * POINTS_PER_SLOT;
        int hostId = hub.getOwnerId();

        boolean updated = userDAO.updateBonusPoints(hostId, pointsToAward);
        if (!updated) {
            throw new SvitloException("Не вдалося нарахувати бонусні бали хосту в базі даних.");
        }

        System.out.println("LOG: Хосту з ID " + hostId + " нараховано " + pointsToAward + " балів за " + bookedSlots + " слотів.");
    }

    /**
     * Логіка обміну балів користувача на знижку партнера.
     * @return true, якщо обмін успішний
     */
    public boolean redeemReward(User user, PartnerReward reward) {
        if (user == null || reward == null) return false;

        int currentPoints = user.getBonusPoints();

        if (currentPoints < reward.getCostInPoints()) {
            System.out.println("LOG: Недостатньо балів для користувача " + user.getUsername());
            return false;
        }

        // Передаємо від'ємне значення балів для їх безпечного списання в БД
        boolean updated = userDAO.updateBonusPoints(user.getId(), -reward.getCostInPoints());
        if (!updated) {
            throw new SvitloException("Помилка сервера: не вдалося списати бали за винагороду.");
        }

        // Синхронізуємо стан об'єкта користувача в пам'яті, щоб UI відразу відобразив оновлений баланс
        user.setBonusPoints(currentPoints - reward.getCostInPoints());

        System.out.println("LOG: Користувач " + user.getUsername() + " успішно придбав '"
                + reward.getTitle() + "' від " + reward.getPartnerName() + ".");

        return true;
    }
}