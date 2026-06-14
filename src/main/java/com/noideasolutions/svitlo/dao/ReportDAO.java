package com.noideasolutions.svitlo.dao;

import com.noideasolutions.svitlo.database.DatabaseConnection;
import com.noideasolutions.svitlo.model.Report;

import java.sql.*;
import java.time.LocalDateTime;

public class ReportDAO {

    // 1. Створити новий запис про скаргу на ХАБ (reported_user_id залишається NULL)
    public boolean save(Report report) {
        String sql = "INSERT INTO reports (reporter_id, hub_id, reported_user_id, reason) VALUES (?, ?, NULL, ?) RETURNING id, created_at";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, report.getReporterId());
            // Оскільки в моделі змінили тип на Integer, використовуємо setObject для безпечного збереження null
            stmt.setObject(2, report.getHubId());
            stmt.setString(3, report.getReason());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    report.setId(rs.getInt("id"));
                    report.setTimestamp(rs.getObject("created_at", LocalDateTime.class));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при створенні скарги на хаб: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // 2. Створити новий запис про скаргу на КОРИСТУВАЧА (hub_id стає NULL)
    public boolean saveUserReport(int reporterId, int reportedUserId, String reason) {
        // Додаємо RETURNING id, created_at, щоб запит виконувався ідентично через executeQuery()
        String sql = "INSERT INTO reports (reporter_id, hub_id, reported_user_id, reason) VALUES (?, NULL, ?, ?) RETURNING id, created_at";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reporterId);
            stmt.setInt(2, reportedUserId);
            stmt.setString(3, reason);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Якщо база повернула згенерований id, значить запис успішний
            }
        } catch (SQLException e) {
            System.err.println("Помилка при створенні скарги на користувача: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при перевірці дублікатів скарг на хаб: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Перевірка, чи не надсилав цей користувач вже скаргу на цього конкретного хоста
    public boolean hasUserAlreadyReportedUser(int reporterId, int reportedUserId) {
        String sql = "SELECT COUNT(*) FROM reports WHERE reporter_id = ? AND reported_user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reporterId);
            stmt.setInt(2, reportedUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при перевірці дублікатів скарг на юзера: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}