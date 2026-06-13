package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.model.User;
import com.noideasolutions.svitlo.service.UserSession;
import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.service.HubService;
import com.noideasolutions.svitlo.util.SceneSwitcher;
import com.noideasolutions.svitlo.service.RadarService;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import java.io.File;
import java.io.IOException;
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

import javax.swing.SwingUtilities;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.awt.view.MapView;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;


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

    @FXML
    private void handleOpenProfileAction(ActionEvent event) {
        SceneSwitcher.switchTo(event, "/com/noideasolutions/svitlo/controller/UserProfile.fxml", "Мій профіль");
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
    private void updateDashboardData() {
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
                if (event.getClickCount() == 2) {
                    int selectedIndex = hubsListView.getSelectionModel().getSelectedIndex();
                    if (selectedIndex >= 0 && !filteredHubs.isEmpty()) {
                        Hub selectedHub = filteredHubs.get(selectedIndex);
                        openHubDetails(event, selectedHub);
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

                TileCache tileCache = new org.mapsforge.map.layer.cache.InMemoryTileCache(500);

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

                tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
                mapView.getLayerManager().getLayers().add(tileRendererLayer);

                mapView.getModel().mapViewPosition.setCenter(new LatLong(50.4501, 30.5234));
                mapView.getModel().mapViewPosition.setZoomLevel((byte) 12);

                javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout());
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

    private void openHubDetails(MouseEvent event, Hub hub) {
        ActionEvent actionEvent = new ActionEvent(event.getSource(), event.getTarget());
        HubDetailsController controller = SceneSwitcher.switchToWithController(
                actionEvent,
                "/com/noideasolutions/svitlo/controller/HubDetails.fxml",
                "Деталі хабу: " + hub.getTitle()
        );
        if (controller != null) {
            controller.setHubData(hub);
        }
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
            try (java.sql.Connection conn = com.noideasolutions.svitlo.database.DatabaseConnection.getConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, newRole);
                pstmt.setInt(2, currentUser.getId());
                pstmt.executeUpdate();
                System.out.println("Роль успішно змінено в БД на: " + newRole);

            } catch (java.sql.SQLException e) {
                System.err.println("Помилка під час виконання SQL-запиту оновлення ролі:");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCreateHubAction(ActionEvent event) {
        SceneSwitcher.switchTo(event, "/com/noideasolutions/svitlo/controller/CreateHub.fxml", "Створення нового хабу");
    }

    @FXML
    public void handleLogoutAction(ActionEvent event) {
        UserSession.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/noideasolutions/svitlo/controller/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 350));
            stage.setTitle("Svitlo++ - Авторизація");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawHubsOnMap(List<Hub> hubsToDraw) {
        if (mapView == null) return;

        // 1. Очищаємо всі старі колірні маркери, але ЗАЛИШАЄМО саму карту (нульовий шар)
        while (mapView.getLayerManager().getLayers().size() > 1) {
            mapView.getLayerManager().getLayers().remove(1);
        }

        Paint fillPaint = AwtGraphicFactory.INSTANCE.createPaint();
        fillPaint.setColor(AwtGraphicFactory.INSTANCE.createColor(150, 255, 0, 0));
        fillPaint.setStyle(Style.FILL);

        Paint strokePaint = AwtGraphicFactory.INSTANCE.createPaint();
        strokePaint.setColor(AwtGraphicFactory.INSTANCE.createColor(255, 0, 0, 0));
        strokePaint.setStrokeWidth(2);
        strokePaint.setStyle(Style.STROKE);

        // 2. Малюємо тільки ті хаби, які пройшли поточні фільтри
        for (Hub hub : hubsToDraw) {
            LatLong latLong = new LatLong(hub.getLatitude(), hub.getLongitude());
            FixedPixelCircle circle = new FixedPixelCircle(latLong, 10, fillPaint, strokePaint);
            mapView.getLayerManager().getLayers().add(circle);
        }

        // Перемальовуємо Swing-компонент
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
}