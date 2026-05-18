package com.noideasolutions.svitlo;

import com.noideasolutions.svitlo.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class MainTest {
    public static void main(String[] args) {
        try {
            System.out.println("Пробуємо підключитися до бази даних...");
            Connection conn = DatabaseConnection.getConnection();

            if (conn != null && !conn.isClosed()) {
                System.out.println("Зв'язок встановлено! Коннект працює.");
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Помилка підключення до бази:");
            e.printStackTrace();
        }
    }
}