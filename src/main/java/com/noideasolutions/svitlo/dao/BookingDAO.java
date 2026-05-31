package com.noideasolutions.svitlo.dao;

import com.noideasolutions.svitlo.database.DatabaseConnection;
import com.noideasolutions.svitlo.model.Booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    // Створити нове бронювання місць у хабі.
    public boolean save(Booking booking) {
        String sql = "INSERT INTO bookings (user_id, hub_id, booked_slots) VALUES (?, ?, ?) RETURNING id, created_at";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, booking.getUserId());
            stmt.setInt(2, booking.getHubId());
            stmt.setInt(3, booking.getBookedSlots());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    booking.setId(rs.getInt("id"));
                    booking.setCreatedAt(rs.getTimestamp("created_at"));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при створенні бронювання: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Знайти всі бронювання конкретного користувача (для екрану "Мої бронювання").
    public List<Booking> findByUserId(int userId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRowToBooking(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні бронювань користувача: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    // Скасувати бронювання (видалити з бази даних).
    public boolean deleteById(int id) {
        String sql = "DELETE FROM bookings WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при видаленні бронювання: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Мапінг рядка з БД в об'єкт Booking
    private Booking mapRowToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setId(rs.getInt("id"));
        booking.setUserId(rs.getInt("user_id"));
        booking.setHubId(rs.getInt("hub_id"));
        booking.setBookedSlots(rs.getInt("booked_slots"));
        booking.setCreatedAt(rs.getTimestamp("created_at"));
        return booking;
    }
}