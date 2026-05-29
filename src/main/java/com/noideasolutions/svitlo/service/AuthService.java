package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.model.User;

public class AuthService {

    // Сюди Вася підключиш свій UserDAO
    // private UserDAO userDAO = new UserDAO();

    /**
     * Логіка реєстрації нового користувача
     */
    public boolean registerUser(String username, String password, String role) {
        if (username == null || username.trim().isEmpty() || password == null || password.length() < 6) {
            System.out.println(" Реєстрація відхилена: невалідний логін або занадто короткий пароль.");
            return false;
        }

        String passwordHash = hashPassword(password);

        User newUser = new User(username, passwordHash, role);
        System.out.println(" Сервіс: Створюємо користувача " + username + " із роллю " + role);

        // Тут буде виклик бази даних: userDAO.save(newUser);
        return true;
    }

    /**
     * Логіка входу в систему
     */
    public User login(String username, String password) {
        System.out.println(" Сервіс: Спроба входу для " + username);

        // Тут буде запит до бази  User user = userDAO.findByUsername(username);
        return null;
    }

    private String hashPassword(String password) {
        // Тимчасово
        return password;
    }
}