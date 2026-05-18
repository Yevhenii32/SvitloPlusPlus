package com.noideasolutions.svitlo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Шаблок
    private static final String URL = "jdbc:postgresql://<neon-host-url>/<dbname>?sslmode=require";
    private static final String USER = "your_username";
    private static final String PASSWORD = "your_password";

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Успішно підключено до Neon PostgreSQL!");
            } catch (ClassNotFoundException e) {
                System.err.println("Драйвер PostgreSQL не знайдено!");
                e.printStackTrace();
            }
        }
        return connection;
    }
}