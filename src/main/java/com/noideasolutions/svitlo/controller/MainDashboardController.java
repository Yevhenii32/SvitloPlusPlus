package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.database.DatabaseConnection;
import com.noideasolutions.svitlo.model.User;
import com.noideasolutions.svitlo.service.UserSession;
import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.service.HubService;
import com.noideasolutions.svitlo.util.SceneSwitcher;
import com.noideasolutions.svitlo.service.RadarService;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.mapsforge.core.model.BoundingBox;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import java.io.File;
import java.net.URL;

import javax.swing.*;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.awt.graphics.AwtBitmap;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.layer.cache.InMemoryTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.awt.view.MapView;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

public class MainDashboardController {

    @FXML
    private Label userInfoLabel;
    @FXML
    private TextField radarRadiusField;

    @FXML
    private ToggleButton radarToggleButton;
    @FXML
    private ToggleButton roleToggleButton;
    @FXML
    private TextField radarLatitudeField;

    @FXML
    private TextField radarLongitudeField;

    @FXML
    private ListView<String> hubsListView;

    @FXML
    private StackPane mapContainer;

    @FXML
    private Button createHubButton;

    @FXML
    private CheckBox filterWifi;
    @FXML
    private CheckBox filterGenerator;
    @FXML
    private CheckBox filterPets;

    private HubService hubService = new HubService();
    private final RadarService radarService = new RadarService();
    private MapView mapView; // Swing-компонент карти

    // КЕШ ВСІХ ХАБІВ ДЛЯ ШВИДКОЇ ФІЛЬТРАЦІЇ
    private List<Hub> allHubs = new ArrayList<>();

    // Статична змінна, щоб зберігати фокус карти між екранами
    private static LatLong lastMapCenter = new LatLong(50.4501, 30.5234);
    private static byte lastZoomLevel = 12;

    @FXML
    private void handleOpenProfileAction(ActionEvent event) {
        try {
            // 1. Завантажуємо FXML профілю користувача
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/noideasolutions/svitlo/controller/UserProfile.fxml"));
            Parent root = loader.load();

            // 2. Створюємо НОВЕ вікно (Stage) для профілю
            Stage profileStage = new Stage();
            profileStage.setScene(new Scene(root));
            profileStage.setTitle("Мій профіль");

            // 3. Встановлюємо поточне головне вікно як власника (щоб мапа залишалася на фоні)
            Stage ownerStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.initOwner(ownerStage);

            // Показуємо вікно профілю. Головне вікно ззаду НЕ пропадає!
            profileStage.show();

        } catch (IOException e) {
            System.err.println("Не вдалося відкрити вікно профілю користувача:");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося відкрити профіль: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        // 1. Отримуємо поточного користувача та перевіряємо, чи він є хостом
        User currentUser = UserSession.getInstance().getCurrentUser();
        boolean isHost = currentUser != null && "HOST".equals(currentUser.getRole());

        if (currentUser != null) {
            userInfoLabel.setText("Користувач: " + currentUser.getUsername() + " | Роль: " + currentUser.getRole());

            if (isHost) {
                roleToggleButton.setText("Режим хоста");
                roleToggleButton.setSelected(true);
            } else {
                roleToggleButton.setText("Режим гостя");
                roleToggleButton.setSelected(false);
            }
        }

        // 2. Керуємо видимістю кнопки створення хабу залежно від ролі користувача
        if (createHubButton != null) {
            createHubButton.setVisible(isHost);
            createHubButton.setManaged(isHost);
        }

        // 3. Завантажуємо хаби з БД в наш кеш-список один раз
        allHubs = hubService.getAllActiveHubs();

        // 4. Заповнюємо список і карту з урахуванням початкового стану фільтрів
        updateDashboardData();

        // 5. Запускаємо ініціалізацію офлайн-карти
        initOfflineMap();
    }

    /**
     * МЕТОД ОБРОБКИ ФІЛЬТРІВ (Викликається при кліку на будь-який чекбокс)
     */
    @FXML
    public void handleFilterAction() {
        updateDashboardData();
    }

    /**
     * Допоміжний метод, який синхронно оновлює і ListView, і карту відповідно до фільтрів
     */
    public void updateDashboardData() {
        boolean needWifi = filterWifi != null && filterWifi.isSelected();
        boolean needGenerator = filterGenerator != null && filterGenerator.isSelected();
        boolean needPets = filterPets != null && filterPets.isSelected();

        // 1. Спочатку спокійно фільтруємо дані в поточному потоці (це безпечно)
        List<Hub> filteredHubs = new ArrayList<>();
        List<String> textItemsToDisplay = new ArrayList<>();

        for (Hub hub : allHubs) {
            if ((needWifi && !hub.isHasWifi()) ||
                    (needGenerator && !hub.isHasGenerator()) ||
                    (needPets && !hub.isAllowsPets())) {
                continue;
            }

            filteredHubs.add(hub);
            String hubInfo = String.format("%s (Вільних місць: %d/%d)",
                    hub.getTitle(), hub.getSlotsAvailable(), hub.getSlotsTotal());
            textItemsToDisplay.add(hubInfo);
        }

        if (textItemsToDisplay.isEmpty()) {
            textItemsToDisplay.add("Немає хабів із вибраними параметрами.");
        }

        // 2. Оновлення JavaFX UI загортаємо в Platform.runLater
        Platform.runLater(() -> {
            hubsListView.getItems().clear();
            hubsListView.getItems().addAll(textItemsToDisplay);

            // Налаштування кліків теж робимо в потоці JavaFX
            hubsListView.setOnMouseClicked((MouseEvent event) -> {
                int selectedIndex = hubsListView.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0 && !filteredHubs.isEmpty()) {
                    Hub selectedHub = filteredHubs.get(selectedIndex);

                    // 1 клік — плавний зум на карті
                    if (event.getClickCount() == 1) {
                        lastMapCenter = new LatLong(selectedHub.getLatitude(), selectedHub.getLongitude());
                        lastZoomLevel = 15;

                        if (mapView != null) {
                            SwingUtilities.invokeLater(() -> {
                                mapView.getModel().mapViewPosition.setCenter(lastMapCenter);
                                mapView.getModel().mapViewPosition.setZoomLevel(lastZoomLevel);
                                mapView.repaint();
                            });
                        }
                    }

                    // ПОДВІЙНИЙ КЛІК — відкриваємо окреме вікно деталей поверх карти
                    if (event.getClickCount() == 2) {
                        openHubDetailsAsNewWindow(selectedHub);
                    }
                }
            });
        });

        // 3. Малювання маркерів карти (це Swing-компонент, тому робимо в SwingUtilities)
        if (mapView != null) {
            SwingUtilities.invokeLater(() -> drawHubsOnMap(filteredHubs));
        }
    }

    private void initOfflineMap() {
        SwingNode swingNode = new SwingNode();
        mapContainer.getChildren().add(swingNode);

        SwingUtilities.invokeLater(() -> {
            try {
                mapView = new MapView();
                mapView.getMapScaleBar().setVisible(true);

                TileCache tileCache = new InMemoryTileCache(500);

                File mapFile = new File("ukraine.map");
                if (!mapFile.exists()) {
                    System.err.println("ПОМИЛКА: Файл ukraine.map не знайдено в кореневій папці проєкту!");
                    return;
                }

                MapDataStore mapDataStore = new MapFile(mapFile);

                TileRendererLayer tileRendererLayer = new TileRendererLayer(
                        tileCache,
                        mapDataStore,
                        mapView.getModel().mapViewPosition,
                        AwtGraphicFactory.INSTANCE
                );

                File themeFile = new File("themes/default.xml");

                // 🔥 Повертаємо вбудований стиль карти
                tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);

                mapView.getLayerManager().getLayers().add(tileRendererLayer);

                // НАШІ ЗМІННІ
                mapView.getModel().mapViewPosition.setCenter(lastMapCenter);
                mapView.getModel().mapViewPosition.setZoomLevel(lastZoomLevel);

                // Захист від крашу та виходу за межі України
                mapView.getModel().mapViewPosition.setZoomLevelMin((byte) 6);  // Не дає віддалити так, щоб побачити сірий фон
                mapView.getModel().mapViewPosition.setZoomLevelMax((byte) 20); // Не дає наблизити до крашу програми

                // Обмеження переміщення (Bounding Box) для України
                // Формат: BoundingBox(minLatitude, minLongitude, maxLatitude, maxLongitude)
                BoundingBox ukraineBox = new BoundingBox(44.38, 22.13, 52.38, 40.22);
                mapView.getModel().mapViewPosition.setMapLimit(ukraineBox);

                JPanel panel = new JPanel(new BorderLayout());
                panel.add(mapView);

                Platform.runLater(() -> swingNode.setContent(panel));

                // Відмальовуємо хаби перший раз (передаємо ті, що пройшли первинний фільтр)
                updateDashboardData();

            } catch (Exception e) {
                System.err.println("Помилка ініціалізації Mapsforge: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void handleRoleSwitch(ActionEvent event) {
        boolean isSelected = roleToggleButton.isSelected();
        String newRole = isSelected ? "HOST" : "GUEST";

        if (isSelected) {
            roleToggleButton.setText("Режим хоста");
        } else {
            roleToggleButton.setText("Режим гостя");
        }

        if (createHubButton != null) {
            createHubButton.setVisible(isSelected);
            createHubButton.setManaged(isSelected);
        }

        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.setRole(newRole);
            userInfoLabel.setText("Користувач: " + currentUser.getUsername() + " | Роль: " + currentUser.getRole());

            String sql = "UPDATE users SET role = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, newRole);
                pstmt.setInt(2, currentUser.getId());
                pstmt.executeUpdate();
                System.out.println("Роль успішно змінено в БД на: " + newRole);

            } catch (SQLException e) {
                System.err.println("Помилка під час виконання SQL-запиту оновлення ролі:");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCreateHubAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/noideasolutions/svitlo/controller/CreateHub.fxml"));
            Parent root = loader.load();

            Stage createStage = new Stage();
            createStage.setScene(new Scene(root));
            createStage.setTitle("Створення нового хабу");

            Stage ownerStage = (Stage) createHubButton.getScene().getWindow();
            createStage.initOwner(ownerStage);

            // 1. КРИТИЧНА ЗМІНА: замість show() використовуємо showAndWait()
            // Цей метод зупиняє виконання коду дашборду на цьому рядку.
            // Дашборд буде просто чекати, поки користувач заповнить поля і закриє вікно створення.
            createStage.showAndWait();

            // 2. ЦЕЙ КОД ВИКОНАЄТЬСЯ АВТОМАТИЧНО ОДРАЗУ ПІСЛЯ ЗАКРИТТЯ ВІКНА СТВОРЕННЯ
            System.out.println("[DEBUG] Вікно створення хабу закрилося. Оновлюємо карту в реальному часі...");

            // Оновлюємо кеш хабів найсвіжішими даними з бази (де вже є новий хаб зі статусом true)
            allHubs = hubService.getAllActiveHubs();

            // Перемальовуємо список та маркери на карті
            updateDashboardData();

        } catch (IOException e) {
            System.err.println("Помилка при відкритті вікна створення хабу:");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogoutAction(ActionEvent event) {
        UserSession.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/noideasolutions/svitlo/controller/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 480, 680));

            stage.centerOnScreen();

            stage.setTitle("Svitlo++ - Авторизація");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawHubsOnMap(List<Hub> hubsToDraw) {
        if (mapView == null) return;

        // 1. Очищаємо всі старі маркери (шари після основного шару карти)
        while (mapView.getLayerManager().getLayers().size() > 1) {
            mapView.getLayerManager().getLayers().remove(1);
        }

        // 2. Створюємо вигляд нашого маркера через стандартний шар AWT
        int radius = 10;

        // Створюємо стандартне AWT зображення в пам'яті
        BufferedImage awtImage = new BufferedImage(
                radius * 2, radius * 2, BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = awtImage.createGraphics();
        // Вмикаємо згладжування, щоб кружечки були красивими й не піксельними
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Малюємо червоне коло (напівпрозоре: новий колір з альфа-каналом)
        g2d.setColor(new Color(255, 0, 0, 150));
        g2d.fillOval(0, 0, radius * 2 - 1, radius * 2 - 1);

        // Малюємо чорний контур
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(0, 0, radius * 2 - 1, radius * 2 - 1);
        g2d.dispose();

        // Загортаємо наше AWT зображення в обгортку Mapsforge Bitmap
        Bitmap bitmap = new AwtBitmap(awtImage);

        // 3. Додаємо маркери на карту
        for (Hub hub : hubsToDraw) {
            LatLong latLong = new LatLong(hub.getLatitude(), hub.getLongitude());

            // Створюємо клікабельний маркер
            Marker marker = new Marker(latLong, bitmap, 0, 0) {
                @Override
                public boolean onTap(LatLong tapLatLong, Point layerPoint, Point tapPoint) {
                    if (contains(layerPoint, tapPoint)) {
                        // 1. ПЛАВНИЙ ЗУМ НА КАРТІ (так само, як при кліку на список)
                        // Оскільки ми в потоці карти, SwingUtilities викликаємо одразу
                        SwingUtilities.invokeLater(() -> {
                            lastMapCenter = new LatLong(hub.getLatitude(), hub.getLongitude());
                            lastZoomLevel = 15; // Рівень зуму, як у списку

                            mapView.getModel().mapViewPosition.setCenter(lastMapCenter);
                            mapView.getModel().mapViewPosition.setZoomLevel(lastZoomLevel);
                            mapView.repaint();
                        });

                        // 2. ВІДКРИТТЯ ВІКНА ДЕТАЛЕЙ (загортаємо в JavaFX потік)
                        Platform.runLater(() -> openHubDetailsAsNewWindow(hub));

                        return true;
                    }
                    return false;
                }
            };

            mapView.getLayerManager().getLayers().add(marker);
        }

        mapView.repaint();
    }

    @FXML
    private void handleRadarToggle(ActionEvent event) {
        User currentUser = UserSession.getInstance().getCurrentUser();

        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "Користувач не авторизований.");
            radarToggleButton.setSelected(false);
            return;
        }

        if (radarToggleButton.isSelected()) {
            try {
                double radiusKm = Double.parseDouble(radarRadiusField.getText());

                if (radiusKm <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Увага", "Радіус має бути більше 0.");
                    radarToggleButton.setSelected(false);
                    return;
                }

                double guestLatitude = Double.parseDouble(radarLatitudeField.getText());
                double guestLongitude = Double.parseDouble(radarLongitudeField.getText());
                radarService.startSearch(
                        currentUser.getId(),
                        guestLatitude,
                        guestLongitude,
                        radiusKm
                );

                radarToggleButton.setText("Вимкнути радар");

                showAlert(Alert.AlertType.INFORMATION,
                        "Радар увімкнено",
                        "Пошук хабів запущено в радіусі " + radiusKm + " км.");

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Помилка", "Введіть коректні  координати та радіус.");
                radarToggleButton.setSelected(false);
            }
        } else {
            radarService.stopSearch();
            radarToggleButton.setText("Увімкнути радар");

            showAlert(Alert.AlertType.INFORMATION,
                    "Радар вимкнено",
                    "Пошук хабів зупинено.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openHubDetailsAsNewWindow(Hub hub) {
        try {
            // 1. Завантажуємо FXML вікна деталей/бронювання
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/noideasolutions/svitlo/controller/HubDetails.fxml"));
            Parent root = loader.load();

            // 2. Передаємо дані хабу в його контролер
            HubDetailsController controller = loader.getController();
            if (controller != null) {
                controller.setHubData(hub);

                // Передаємо посилання на цей дашборд, щоб вікно деталей могло сказати "оновися", коли користувач забронює місце
                controller.setMainDashboardController(this);
            }

            // 3. Створюємо і відкриваємо НОВЕ вікно поверх старого
            Stage detailsStage = new Stage();
            detailsStage.setScene(new Scene(root));
            detailsStage.setTitle("Бронювання хабу: " + hub.getTitle());

            // Встановлюємо власником головне вікно (щоб воно було на фоні)
            if (hubsListView != null && hubsListView.getScene() != null) {
                Stage ownerStage = (Stage) hubsListView.getScene().getWindow();
                detailsStage.initOwner(ownerStage);
            }

            // Показуємо вікно. Карта ззаду залишається видимою і не закривається!
            detailsStage.show();

        } catch (IOException e) {
            System.err.println("Не вдалося відкрити вікно деталей хабу:");
            e.printStackTrace();
        }
    }
}