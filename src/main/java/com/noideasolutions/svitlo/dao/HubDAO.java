package com.noideasolutions.svitlo.dao;

import com.noideasolutions.svitlo.database.DatabaseConnection;
import com.noideasolutions.svitlo.model.Hub;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HubDAO {

    // Збереження нового хабу
    public boolean save(Hub hub) {
        String sql = "INSERT INTO hubs (host_id, title, description, latitude, longitude, slots_total, slots_available, is_active, has_wifi, has_generator, allows_pets) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id, created_at";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, hub.getHostId());
            stmt.setString(2, hub.getTitle());
            stmt.setString(3, hub.getDescription());
            stmt.setDouble(4, hub.getLatitude());
            stmt.setDouble(5, hub.getLongitude());
            stmt.setInt(6, hub.getSlotsTotal());
            stmt.setInt(7, hub.getSlotsAvailable());
            stmt.setBoolean(8, hub.isActive());
            stmt.setBoolean(9, hub.isHasWifi());
            stmt.setBoolean(10, hub.isHasGenerator());
            stmt.setBoolean(11, hub.isAllowsPets());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    hub.setId(rs.getInt("id"));
                    hub.setCreatedAt(rs.getTimestamp("created_at"));
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при збереженні хабу: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Отримання всіх активних хабів
    public List<Hub> findAllActive() {
        List<Hub> hubs = new ArrayList<>();
        String sql = "SELECT * FROM hubs WHERE is_active = true ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                hubs.add(mapRowToHub(rs));
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні активних хабів: " + e.getMessage());
            e.printStackTrace();
        }
        return hubs;
    }

    // Пошук хабу за ID
    public Hub findById(int id) {
        String sql = "SELECT * FROM hubs WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToHub(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при пошуку хабу за ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Оновлення хабу
    public boolean update(Hub hub) {
        String sql = "UPDATE hubs SET title = ?, description = ?, latitude = ?, longitude = ?, " +
                "slots_total = ?, slots_available = ?, is_active = ?, has_wifi = ?, has_generator = ?, allows_pets = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hub.getTitle());
            stmt.setString(2, hub.getDescription());
            stmt.setDouble(3, hub.getLatitude());
            stmt.setDouble(4, hub.getLongitude());
            stmt.setInt(5, hub.getSlotsTotal());
            stmt.setInt(6, hub.getSlotsAvailable());
            stmt.setBoolean(7, hub.isActive());
            stmt.setBoolean(8, hub.isHasWifi());
            stmt.setBoolean(9, hub.isHasGenerator());
            stmt.setBoolean(10, hub.isAllowsPets());

            stmt.setInt(11, hub.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при оновленні хабу: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Отримання активних хабів, у яких є вільні місця
    public List<Hub> findAvailableHubs() {
        List<Hub> hubs = new ArrayList<>();
        String sql = "SELECT * FROM hubs WHERE is_active = true AND slots_available > 0 ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                hubs.add(mapRowToHub(rs));
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні доступних хабів для карти: " + e.getMessage());
            e.printStackTrace();
        }
        return hubs;
    }

    // Мапінг рядка з БД в об'єкт Hub (додано читання нових полей)
    private Hub mapRowToHub(ResultSet rs) throws SQLException {
        Hub hub = new Hub();
        hub.setId(rs.getInt("id"));
        hub.setHostId(rs.getInt("host_id"));
        hub.setTitle(rs.getString("title"));
        hub.setDescription(rs.getString("description"));
        hub.setLatitude(rs.getDouble("latitude"));
        hub.setLongitude(rs.getDouble("longitude"));
        hub.setSlotsTotal(rs.getInt("slots_total"));
        hub.setSlotsAvailable(rs.getInt("slots_available"));
        hub.setActive(rs.getBoolean("is_active"));
        hub.setCreatedAt(rs.getTimestamp("created_at"));

        // Зчитуємо наші нові колонки з бази даних
        hub.setHasWifi(rs.getBoolean("has_wifi"));
        hub.setHasGenerator(rs.getBoolean("has_generator"));
        hub.setAllowsPets(rs.getBoolean("allows_pets"));
        return hub;
    }
}