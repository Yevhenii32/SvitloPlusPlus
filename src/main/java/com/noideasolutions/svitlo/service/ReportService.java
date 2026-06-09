package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.dao.HubDAO;
import com.noideasolutions.svitlo.dao.ReportDAO;
import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.model.Report;
import com.noideasolutions.svitlo.exception.SvitloException;

public class ReportService {

    private final ReportDAO reportDAO;
    private final HubDAO hubDAO;
    private static final int MAX_REPORTS_THRESHOLD = 3;

    public ReportService() {
        this.reportDAO = new ReportDAO();
        this.hubDAO = new HubDAO();
    }

    /**
     * Обробляє нову скаргу від користувача та автоматично модерує хаб.
     */
    public void submitReport(int reporterId, Hub hub, String reason) {
        if (hub == null || reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Некоректні дані для скарги.");
        }

        // Створюємо та зберігаємо об'єкт скарги в БД
        Report newReport = new Report(0, reporterId, hub.getId(), reason);
        boolean saved = reportDAO.save(newReport);

        if (!saved) {
            throw new SvitloException("Помилка сервера: не вдалося зберегти скаргу в базу даних.");
        }

        // Збільшуємо лічильник скарг хабу в пам'яті
        int currentCount = hub.getReportCount();
        hub.setReportCount(currentCount + 1);

        // Перевіряємо, чи не час блокувати хаб за флуд/неадекватність
        if (hub.getReportCount() >= MAX_REPORTS_THRESHOLD) {
            hub.setActive(false);
            System.out.println(" СИСТЕМА БЕЗПЕКИ: Хаб '" + hub.getTitle() + "' АВТОМАТИЧНО ЗАБЛОКОВАНО! (Скарг: " + hub.getReportCount() + ")");
        }

        // Синхронізуємо новий стан хабу (кількість скарг та статус активності) з базою даних
        boolean updated = hubDAO.update(hub);
        if (!updated) {
            throw new SvitloException("Помилка сервера: не вдалося оновити статус хабу після фіксації скарги.");
        }
    }
}