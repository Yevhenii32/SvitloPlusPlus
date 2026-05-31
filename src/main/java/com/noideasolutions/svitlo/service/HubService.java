package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.dao.HubDAO;
import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.model.User;

import java.util.List;

public class HubService {

    private HubDAO hubDAO;

    public HubService() {
        this.hubDAO = new HubDAO();
    }

    /**
     * Створення нового хабу (Доступно тільки для Хостів)
     */
    public boolean createHub(String title, String description, double latitude, double longitude, int slotsTotal) {
        // Отримуємо поточного користувача з глобальної сесії програми
        User currentUser = UserSession.getInstance().getCurrentUser();

        // Перевірка 1: Чи користувач взагалі залогінений?
        if (currentUser == null) {
            System.out.println(" Помилка: Користувач не авторизований. Створення хабу відхилено.");
            return false;
        }

        // Перевірка 2: Чи має він права Хоста?
        if (!"HOST".equalsIgnoreCase(currentUser.getRole())) {
            System.out.println(" Помилка: Права доступу відсутні. Тільки HOST може створювати хаби.");
            return false;
        }

        // Перевірка 3: Валідація даних
        if (title == null || title.trim().isEmpty() || slotsTotal <= 0) {
            System.out.println(" Помилка: Назва хабу не може бути порожньою, а кількість місць має бути > 0.");
            return false;
        }

        // Створюємо об'єкт хабу. ID хоста беремо з сесії
        Hub newHub = new Hub(currentUser.getId(), title, description, latitude, longitude, slotsTotal);

        // Зберігаємо в базу через DAO Василя
        boolean isSaved = hubDAO.save(newHub);

        if (isSaved) {
            System.out.println(" Хаб '" + title + "' успішно створено та додано на карту!");
            return true;
        } else {
            System.out.println(" Помилка бази даних при збереженні хабу.");
            return false;
        }
    }

    /**
     * Отримання списку всіх активних хабів для відображення на головному екрані/карті
     */
    public List<Hub> getAllActiveHubs() {
        System.out.println(" Завантажуємо список доступних хабів з бази...");
        return hubDAO.findAllActive();
    }

    /**
     * Пошук конкретного хабу за ID (знадобиться для сторінки деталей хабу)
     */
    public Hub getHubById(int id) {
        return hubDAO.findById(id);
    }
}