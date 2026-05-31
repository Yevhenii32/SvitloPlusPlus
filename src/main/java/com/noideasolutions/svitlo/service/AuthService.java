package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.dao.UserDAO;
import com.noideasolutions.svitlo.model.User;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    // Підключаємо DAO для роботи з базою
    private UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Реєстрація нового користувача
     */
    public boolean registerUser(String username, String password, String role) {
        // Базова валідація
        if (username == null || username.trim().isEmpty() || password == null || password.length() < 4) {
            System.out.println(" Реєстрація відхилена: порожній логін або занадто короткий пароль.");
            return false;
        }

        // Перевіряємо, чи не зайнятий такий логін
        if (userDAO.findByUsername(username) != null) {
            System.out.println(" Реєстрація відхилена: користувач з таким іменем вже існує.");
            return false;
        }

        // Хешуємо пароль
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

        // Створюємо об'єкт користувача з хешем замість реального пароля
        User newUser = new User(username, passwordHash, role);

        // Зберігаємо в базу даних
        boolean isSaved = userDAO.save(newUser);

        if (isSaved) {
            System.out.println(" Користувача '" + username + "' успішно зареєстровано в БД!");
            return true;
        } else {
            System.out.println(" Помилка при збереженні в базу даних.");
            return false;
        }
    }

    /**
     * Авторизація (Вхід у систему)
     */
    public User login(String username, String password) {
        System.out.println(" Спроба входу для '" + username + "'...");

        // Шукаємо користувача в БД
        User user = userDAO.findByUsername(username);

        if (user == null) {
            System.out.println(" Помилка: Користувача не знайдено.");
            return null;
        }

        //  Порівнюємо введений пароль із хешем із бази
        if (BCrypt.checkpw(password, user.getPasswordHash())) {
            System.out.println(" Вхід успішний! Вітаємо, " + username + " (Роль: " + user.getRole() + ")");
            return user;
        } else {
            System.out.println(" Помилка: Неправильний пароль.");
            return null;
        }
    }
}