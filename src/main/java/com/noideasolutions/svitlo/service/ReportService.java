package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.dao.HubDAO;
import com.noideasolutions.svitlo.dao.ReportDAO;
import com.noideasolutions.svitlo.dao.UserDAO;
import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.model.Report;
import com.noideasolutions.svitlo.model.User;
import com.noideasolutions.svitlo.exception.SvitloException;

/**
 * Сервіс модерації та безпеки, що обробляє скарги користувачів у системі "Svitlo++".
 * Реалізує автоматичне блокування акаунтів порушників та деактивацію їхніх хабів при досягненні ліміту скарг.
 */
public class ReportService {

    private final ReportDAO reportDAO;
    private final HubDAO hubDAO;
    private final UserDAO userDAO; // ДОДАНО: DAO для роботи з користувачами
    private static final int MAX_REPORTS_THRESHOLD = 3;

    public ReportService() {
        this.reportDAO = new ReportDAO();
        this.hubDAO = new HubDAO();
        this.userDAO = new UserDAO(); // Ініціалізуємо
    }

    /**
     * Фіксує скаргу на хаб, оновлює його лічильник та збільшує кількість страйків власника.
     * При досягненні ліміту автоматично блокує хоста та приховує всі його хаби з мапи додатку.
     */
    public void submitReport(int reporterId, Hub hub, String reason) {
        if (hub == null || reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Некоректні дані для скарги.");
        }

        // Заборона скаржитися на самого себе
        if (reporterId == hub.getHostId()) {
            throw new SvitloException("Ви не можете надіслати скаргу на власний хаб.");
        }

        // Захист від спаму скаргами
        if (reportDAO.hasUserAlreadyReported(reporterId, hub.getId())) {
            throw new SvitloException("Ви вже надсилали скаргу на цей хаб. Дякуємо за пильність!");
        }

        // 1. Створюємо та зберігаємо об'єкт скарги в БД
        Report newReport = new Report(0, reporterId, hub.getId(), reason);
        boolean saved = reportDAO.save(newReport);

        if (!saved) {
            throw new SvitloException("Помилка сервера: не вдалося зберегти скаргу в базу даних.");
        }

        // 2. Оновлюємо лічильник скарг для хабу
        int currentHubCount = hub.getReportCount();
        hub.setReportCount(currentHubCount + 1);
        boolean hubUpdated = hubDAO.update(hub);

        if (!hubUpdated) {
            throw new SvitloException("Помилка сервера: не вдалося оновити статус хабу після фіксації скарги.");
        }

        // 3. Шукаємо власника хабу і оновлюємо його "карму"
        User host = userDAO.findById(hub.getHostId());

        if (host != null) {
            int currentUserComplaints = host.getComplaintsCount();
            host.setComplaintsCount(currentUserComplaints + 1);

            // Перевіряємо, чи не час блокувати акаунт порушника
            if (host.getComplaintsCount() >= MAX_REPORTS_THRESHOLD) {
                host.setBlocked(true); // Блокуємо акаунт

                // Зносимо всі хаби цього користувача
                boolean hubsDeactivated = hubDAO.deactivateAllByHostId(host.getId());
                if (hubsDeactivated) {
                    System.out.println(" Всі хаби порушника прибрано.");
                }

                System.out.println(" СИСТЕМА БЕЗПЕКИ: Користувача '" + host.getUsername() + "' АВТОМАТИЧНО ЗАБЛОКОВАНО!");
            }
            // Зберігаємо оновлені дані користувача (кількість скарг та можливий бан) в БД
            boolean hostUpdated = userDAO.update(host);
            if (!hostUpdated) {
                throw new SvitloException("Помилка сервера: не вдалося оновити статус користувача.");
            }
        }
    }

    /**
     * Реєструє скаргу безпосередньо на профіль користувача (хоста).
     * Нараховує штрафний бал та запускає процедуру автоматичного бану і приховування хабів у разі перевищення ліміту.
     */
    public void submitUserReport(int reporterId, int reportedUserId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Причина скарги не може бути порожньою.");
        }

        if (reporterId == reportedUserId) {
            throw new SvitloException("Ви не можете надіслати скаргу на самого себе.");
        }

        // 1. Захист від спаму скаргами (на рівні профілю)
        if (reportDAO.hasUserAlreadyReportedUser(reporterId, reportedUserId)) {
            throw new SvitloException("Ви вже надсилали скаргу на цього користувача.");
        }

        // 2. Створюємо об'єкт скарги
        // Оскільки скарга йде на користувача, в полі hub_id передаємо 0 або NULL (залежно від конструктора Report)
        Report newReport = new Report(0, reporterId, 0, reason);


        boolean saved = reportDAO.saveUserReport(reporterId, reportedUserId, reason);

        if (!saved) {
            throw new SvitloException("Помилка сервера: не вдалося зберегти скаргу на користувача.");
        }

        // 3. Шукаємо порушника в БД і нараховуємо йому штрафний бал
        User host = userDAO.findById(reportedUserId);

        if (host != null) {
            int currentComplaints = host.getComplaintsCount();
            host.setComplaintsCount(currentComplaints + 1);

            // Перевіряємо автоматичний бан за накопичені скарги
            if (host.getComplaintsCount() >= MAX_REPORTS_THRESHOLD) {
                host.setBlocked(true); // Бан!

                // Автоматично прибираємо всі його хаби з карти
                boolean hubsDeactivated = hubDAO.deactivateAllByHostId(host.getId());
                if (hubsDeactivated) {
                    System.out.println(" СИСТЕМА БЕЗПЕКИ: Всі хаби заблокованого хоста прибрано.");
                }

                System.out.println(" СИСТЕМА БЕЗПЕКИ: Користувача '" + host.getUsername() + "' АВТОМАТИЧНО ЗАБЛОКОВАНО через скаргу на профіль!");
            }

            // Зберігаємо оновленого юзера (нові скарги або бан) у БД
            boolean hostUpdated = userDAO.update(host);
            if (!hostUpdated) {
                throw new SvitloException("Помилка сервера: не вдалося оновити статус користувача.");
            }
        } else {
            throw new SvitloException("Помилка: користувача, на якого ви скаржитесь, не знайдено в базі даних.");
        }
    }
}