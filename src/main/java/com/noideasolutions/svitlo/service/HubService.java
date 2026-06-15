package com.noideasolutions.svitlo.service;

import com.noideasolutions.svitlo.dao.HubDAO;
import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.model.User;

import java.util.List;

/**
 * Сервіс для управління хабами у системі.
 * Забезпечує перевірку прав доступу хостів, валідацію параметрів хабів та отримання їхніх списків із БД.
 */
public class HubService {

    private HubDAO hubDAO;

    public HubService() {
        this.hubDAO = new HubDAO();
    }

    /**
     * Перевіряє авторизацію та роль користувача, валідує вхідні дані та реєструє новий хаб у системі.
     * Автоматично пов'язує створюваний хаб із унікальним ідентифікатором поточного хоста із сесії.
     */
    public boolean createHub(String title, String description, double latitude, double longitude, int slotsTotal,
                             boolean hasWifi, boolean hasGenerator, boolean allowsPets) {
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

        // Створюємо об'єкт хабу
        Hub newHub = new Hub(currentUser.getId(), title, description, latitude, longitude, slotsTotal);

        // Встановлюємо нові параметри
        newHub.setHasWifi(hasWifi);
        newHub.setHasGenerator(hasGenerator);
        newHub.setAllowsPets(allowsPets);

        // Зберігаємо в базу через DAO
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
     * Повертає список усіх активних хабів, що доступні для відображення на інтерактивній карті.
     * Використовується для первинного завантаження дашборду.
     */
    public List<Hub> getAllActiveHubs() {
        System.out.println(" Завантажуємо список доступних хабів з бази...");
        return hubDAO.findAllActive();
    }

    /**
     * Виконує пошук конкретного хабу за його унікальним ідентифікатором.
     * Використовується для отримання детальної інформації при кліку на маркер карти.
     */
    public Hub getHubById(int id) {
        return hubDAO.findById(id);
    }
}