package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.model.Report;

public class ReportService {

    // Максимальна кількість скарг до автоматичного блокування
    private static final int MAX_REPORTS_THRESHOLD = 3;

    /**
     * Обробляє нову скаргу від користувача.
     */
    public void submitReport(int reporterId, Hub hub, String reason) {
        if (hub == null || reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Некоректні дані для скарги.");
        }

        // 1. Створюємо об'єкт скарги
        Report newReport = new Report(0, reporterId, hub.getId(), reason);
        System.out.println("LOG: Отримано скаргу на хаб '" + hub.getTitle() + "'. Причина: " + reason);

        // TODO: Зберегти скаргу в базу даних через ReportDAO
        // reportDAO.save(newReport);

        // 2. Збільшуємо лічильник скарг хабу
        int currentCount = hub.getReportCount();
        hub.setReportCount(currentCount + 1);

        // 3. Перевіряємо, чи не час блокувати хаб
        if (hub.getReportCount() >= MAX_REPORTS_THRESHOLD) {
            hub.setActive(false);
            System.out.println(" СИСТЕМА БЕЗПЕКИ: Хаб '" + hub.getTitle() + "' АВТОМАТИЧНО ЗАБЛОКОВАНО! (Досягнуто ліміт скарг: " + hub.getReportCount() + ")");
        }

        // TODO: Оновити стан хабу в базі даних (кількість скарг і статус активності)
        // hubDAO.update(hub);
    }
}