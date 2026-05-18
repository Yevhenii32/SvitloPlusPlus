package com.noideasolutions.svitlo.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static Connection connection = null;

    // Метод, який читає файл db.properties
    private static String getDbUrl() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("db.properties")) {
            props.load(in);
            return props.getProperty("db.url");
        } catch (IOException e) {
            System.err.println("Не знайдено файл db.properties! Створіть його в корені проєкту.");
            return null;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = getDbUrl();
            if (url == null) throw new SQLException("URL бази даних порожній.");

            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(url);
                System.out.println("Успішно підключено до Neon PostgreSQL (безпечний режим)!");
            } catch (ClassNotFoundException e) {
                System.err.println("Драйвер PostgreSQL не знайдено!");
                e.printStackTrace();
            }
        }
        return connection;
    }
}