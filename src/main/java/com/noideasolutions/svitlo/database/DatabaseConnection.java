package com.noideasolutions.svitlo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://ep-lucky-rain-al41w67r-pooler.c-3.eu-central-1.aws.neon.tech/neondb?user=neondb_owner&password=npg_YfjuXKas10rM&sslmode=require&channelBinding=require";

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL);
                System.out.println("Успішно підключено до Neon PostgreSQL!");
            } catch (ClassNotFoundException e) {
                System.err.println("Драйвер PostgreSQL не знайдено!");
                e.printStackTrace();
            }
        }
        return connection;
    }
}