package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.model.User;

/**
 * Сервіс для керування сесією поточного користувача в межах додатка.
 * Реалізує шаблон Singleton для забезпечення єдиної точки доступу до даних авторизованого користувача
 * та збереження стану його автентифікації в оперативній пам'яті.
 */
public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {}

    /**
     * Метод для отримання єдиного об'єкта сесії.
     * Створює об'єкт лише при першому виклику,
     * а при наступних — повертає вже створений.
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Встановлює користувача, який успішно увійшов у систему.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Повертає дані поточного авторизованого користувача.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Завершує поточну сесію користувача,
     * очищуючи дані про нього.
     */
    public void logout() {
        this.currentUser = null;
    }
}