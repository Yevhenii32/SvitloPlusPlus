package com.noideasolutions.svitlo.dao;

import com.noideasolutions.svitlo.database.DatabaseConnection;
import com.noideasolutions.svitlo.model.Report;

import java.sql.*;
import java.time.LocalDateTime;

public class ReportDAO {

    // Створити новий запис про скаргу на хаб.
    public boolean save(Report report) {
        // Додаємо RETURNING id, timestamp, щоб синхронізувати об'єкт з БД після запису
        String sql = "INSERT INTO reports (reporter_id, hub_id, reason) VALUES (?, ?, ?) RETURNING id, created_at";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, report.getReporterId());
            stmt.setInt(2, report.getHubId());
            stmt.setString(3, report.getReason());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Записуємо згенеровані БД дані назад в об'єкт
                    report.setId(rs.getInt("id"));
                    // Нові версії JDBC дозволяють діставати LocalDateTime напряму
                    report.setTimestamp(rs.getObject("created_at", LocalDateTime.class));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при створенні скарги: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    // Перевірка, чи не надсилав цей користувач вже скаргу на цей конкретний хаб
    public boolean hasUserAlreadyReported(int reporterId, int hubId) {
        String sql = "SELECT COUNT(*) FROM reports WHERE reporter_id = ? AND hub_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reporterId);
            stmt.setInt(2, hubId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Якщо кількість більша за 0, значить скарга вже є
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при перевірці дублікатів скарг: " + e.getMessage());
            e.printStackTrace();
        }
        return false; // За замовчуванням дозволяємо, якщо сталась помилка
    }
}