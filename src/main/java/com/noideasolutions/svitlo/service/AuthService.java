package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.dao.UserDAO;
import com.noideasolutions.svitlo.model.User;
import com.noideasolutions.svitlo.exception.DuplicateUserException;
import com.noideasolutions.svitlo.exception.InvalidCredentialsException;
import com.noideasolutions.svitlo.exception.SvitloException;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Сервіс для управління процесами автентифікації, реєстрації та безпеки користувачів.
 * Відповідає за безпечне хешування паролів та перевірку статусів блокування акаунтів.
 */
public class AuthService {

    private UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Реєструє нового користувача в системі, використовуючи алгоритм BCrypt для шифрування пароля.
     * Запобігає дублюванню нікнеймів та кидає виняток, якщо запис у базу даних завершився невдачею.
     */
    public void registerUser(String username, String password, String role) {
        // Базова валідація залишається стандартним винятком
        if (username == null || username.trim().isEmpty() || password == null || password.length() < 4) {
            throw new IllegalArgumentException("Порожній логін або пароль коротший за 4 символи.");
        }

        // ВИКОРИСТОВУЄМО НАШ КАСТОМНИЙ ВИНЯТОК
        if (userDAO.findByUsername(username) != null) {
            throw new DuplicateUserException("Користувач з іменем '" + username + "' вже існує.");
        }

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        User newUser = new User(username, passwordHash, role);

        boolean isSaved = userDAO.save(newUser);

        // Якщо база даних "впала", кидаємо базовий SvitloException
        if (!isSaved) {
            throw new SvitloException("Внутрішня помилка сервера: не вдалося зберегти користувача.");
        }

        System.out.println(" Користувача '" + username + "' успішно зареєстровано в БД!");
    }

    /**
     * Проводить авторизацію користувача через перевірку відповідності введеного пароля його хешу.
     * Валідує статус блокування облікового запису перед наданням доступу до системи.
     */
    public User login(String username, String password) {
        System.out.println(" Спроба входу для '" + username + "'...");

        User user = userDAO.findByUsername(username);

        if (user == null || !BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Неправильний логін або пароль.");
        }

        // Чи не заблокований цей користувач?
        if (user.isBlocked()) {
            System.out.println(" Відмовлено у доступі: акаунт '" + username + "' заблоковано системою безпеки.");
            throw new SvitloException("Ваш акаунт перманентно заблоковано за порушення правил (3 скарги).");
        }

        System.out.println(" Вхід успішний! Вітаємо, " + username + " (Роль: " + user.getRole() + ")");
        return user;
    }
}