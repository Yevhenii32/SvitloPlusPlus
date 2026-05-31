package com.noideasolutions.svitlo.dao;

import com.noideasolutions.svitlo.database.DatabaseConnection;
import com.noideasolutions.svitlo.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Створення або збереження користувача
    public boolean save(User user) {
        String sql = "INSERT INTO users (username, password_hash, role, rating, bonus_points, complaints_count, is_blocked) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());
            stmt.setDouble(4, user.getRating());
            stmt.setInt(5, user.getBonusPoints());
            stmt.setInt(6, user.getComplaintsCount());
            stmt.setBoolean(7, user.isBlocked());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getInt("id")); // Зберігаємо згенерований БД ID об'єкту
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при збереженні користувача: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Пошук за юзернеймом для AuthService.login
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при пошуку користувача за ім'ям: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Пошук за ID (знадобиться для перевірки хоста у хабах)
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при пошуку користувача за ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Оновлення даних користувача (рейтинг, бонуси, блок)
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, password_hash = ?, role = ?, rating = ?, " +
                "bonus_points = ?, complaints_count = ?, is_blocked = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());
            stmt.setDouble(4, user.getRating());
            stmt.setInt(5, user.getBonusPoints());
            stmt.setInt(6, user.getComplaintsCount());
            stmt.setBoolean(7, user.isBlocked());
            stmt.setInt(8, user.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при оновленні користувача: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Мапінг рядка з БД в об'єкт User
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setRating(rs.getDouble("rating"));
        user.setBonusPoints(rs.getInt("bonus_points"));
        user.setComplaintsCount(rs.getInt("complaints_count"));
        user.setBlocked(rs.getBoolean("is_blocked"));
        return user;
    }
}