package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.dao.HubDAO;
import com.noideasolutions.svitlo.dao.UserDAO;
import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.model.User;
import com.noideasolutions.svitlo.service.ReportService;
import com.noideasolutions.svitlo.service.UserSession;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * Контролер для керування вікном профілю хоста (власника хабу).
 * Забезпечує асинхронне завантаження даних про власника та його активні об'єкти з БД,
 * а також надає можливість відкрити прямий чат або надіслати скаргу модераторам.
 */
public class HostProfileController {

    @FXML private Label hostNameLabel;
    @FXML private Label hostRatingLabel;
    @FXML private ListView<String> hostHubsListView;

    private final UserDAO userDAO = new UserDAO();
    private final HubDAO hubDAO = new HubDAO(); // ІНІЦІАЛІЗАЦІЯ DAO ДЛЯ ХАБІВ
    private final ReportService reportService = new ReportService();
    private int hostId;

    /**
     * Встановлює ID хоста. Цей метод викликається з боку HubDetailsController
     * при переході на профіль власника. Одразу запускає завантаження даних.
     */
    public void setHostId(int hostId) {
        this.hostId = hostId;
        loadHostProfileData();
    }

    /**
     * Асинхронно завантажує інформацію про профіль користувача та його хаби.
     * Використовує окремий потік для запитів до БД, щоб уникнути фризів UI,
     * та повертає керування в потік JavaFX через Platform.runLater для оновлення сцени.
     */
    private void loadHostProfileData() {
        // Завантажуємо дані у фоновому потоці, щоб UI додатка не зависав
        new Thread(() -> {
            // 1. Дістаємо хоста з БД через UserDAO
            User host = userDAO.findById(hostId);

            // 2. Отримуємо РЕАЛЬНІ хаби цього хоста за допомогою нового методу з HubDAO
            List<Hub> ownerHubs = hubDAO.findByHostId(hostId);

            // Повертаємось у потік JavaFX для безпечного оновлення UI
            Platform.runLater(() -> {
                if (host != null) {
                    hostNameLabel.setText(host.getUsername());
                    hostRatingLabel.setText(String.format("⭐ %.1f / 5.0", host.getRating()));
                } else {
                    hostNameLabel.setText("Власник #" + hostId);
                    hostRatingLabel.setText("⭐ 0.0");
                }

                // Очищаємо список перед заповненням
                hostHubsListView.getItems().clear();

                if (ownerHubs.isEmpty()) {
                    hostHubsListView.getItems().add("Цей власник ще не створив жодного активного хабу.");
                } else {
                    for (Hub h : ownerHubs) {
                        // Рендеримо красивий рядок із реальними даними
                        hostHubsListView.getItems().add(
                                String.format("🏠 %s (Місць: %d/%d)",
                                        h.getTitle(), h.getSlotsAvailable(), h.getSlotsTotal())
                        );
                    }
                }
            });
        }).start();
    }

    /**
     * Обробник події кліку на кнопку "Написати".
     * Перевіряє авторизацію поточного користувача, захищає від створення чату із самим собою,
     * ініціалізує сесію тимчасового чату та відкриває нове вікно листування.
     */
    @FXML
    private void handleChatAction(ActionEvent event) {
        try {
            User currentUser = UserSession.getInstance().getCurrentUser();

            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Помилка", "Користувач не авторизований.");
                return;
            }

            if (currentUser.getId() == hostId) {
                showAlert(Alert.AlertType.WARNING, "Увага", "Ви не можете відкрити чат із самим собою.");
                return;
            }

            ChatWindowController.getChatService()
                    .confirmRequestAndOpenChat(currentUser.getId(), hostId);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/noideasolutions/svitlo/controller/ChatWindow.fxml"
            ));
            Parent root = loader.load();

            ChatWindowController chatController = loader.getController();
            chatController.setChatData(currentUser.getId(), hostId);

            Stage stage = new Stage();
            stage.setTitle("Чат з хостом");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося відкрити чат: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Обробник події кліку на кнопку "Поскаржитись".
     * Відкриває діалогове вікно для введення причини скарги, валідує текст,
     * відправляє запит у ReportService та гнучко обробляє можливі помилки бази даних.
     */
    @FXML
    private void handleReportAction(ActionEvent event) {
        try {
            User currentUser = UserSession.getInstance().getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Помилка", "Користувач не авторизований.");
                return;
            }

            if (currentUser.getId() == hostId) {
                showAlert(Alert.AlertType.WARNING, "Увага", "Ви не можете поскаржитися на самого себе.");
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Скарга на користувача");
            dialog.setHeaderText("Повідомити про порушення з боку хоста\n(ID користувача: " + hostId + ")");
            dialog.setContentText("Опишіть причину вашої скарги:");

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(reason -> {
                if (reason.trim().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Увага", "Причина скарги не може бути порожньою!");
                    return;
                }

                try {
                    reportService.submitUserReport(currentUser.getId(), hostId, reason);

                    showAlert(Alert.AlertType.INFORMATION, "Скаргу надіслано",
                            "Вашу скаргу на хоста успішно зафіксовано. Модератори перевірять профіль.");

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.close();

                    // 1. ПЕРЕХОПЛЮЄМО ВАШУ БІЗНЕС-ПОМИЛКУ (Потрібно буде імпортувати com.noideasolutions.svitlo.exception.SvitloException, якщо він в іншому пакеті)
                } catch (com.noideasolutions.svitlo.exception.SvitloException e) {
                    // Показуємо користувачеві чітке попередження без закриття вікна (текст: "Ви вже надсилали скаргу...")
                    showAlert(Alert.AlertType.WARNING, "Увага", e.getMessage());

                    // 2. ПЕРЕХОПЛЮЄМО ВСЕ ІНШЕ (проблеми з PostgreSQL, мережею тощо)
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Помилка сервера", "Не вдалося надіслати скаргу: " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Щось пішло не так: " + e.getMessage());
        }
    }

    /**
     * Обробник події кліку на кнопку "Закрити" або "Назад".
     * Закриває поточний Stage профілю та повертає фокус на попереднє вікно деталей хабу.
     */
    @FXML
    private void handleCloseAction(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Внутрішній допоміжний метод для створення та виклику вікон сповіщень (Alert Dialogs).
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}