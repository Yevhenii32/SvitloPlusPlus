package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.model.User;
import com.noideasolutions.svitlo.service.UserSession;
import com.noideasolutions.svitlo.model.Hub;
import com.noideasolutions.svitlo.service.HubService;
import com.noideasolutions.svitlo.util.SceneSwitcher;

import java.io.File;
import java.io.IOException;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.swing.SwingUtilities;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.awt.util.AwtUtil;
import org.mapsforge.map.awt.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;

public class MainDashboardController {

    @FXML
    private Label userInfoLabel;

    @FXML
    private ToggleButton roleToggleButton;

    @FXML
    private ListView<String> hubsListView;

    @FXML
    private StackPane mapContainer;

    @FXML
    private Button createHubButton; // <-- ДОДАТИ ЦЕЙ РЯДОК (імпортуй javafx.scene.control.Button)

    private HubService hubService = new HubService();
    private MapView mapView; // Swing-компонент карти

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

        // 3. Отримуємо реальні хаби з бази даних
        List<Hub> activeHubs = hubService.getAllActiveHubs();

        // Очищаємо список перед оновленням
        hubsListView.getItems().clear();

        if (activeHubs.isEmpty()) {
            hubsListView.getItems().add("Наразі немає доступних хабів зі світлом.");
        } else {
            for (Hub hub : activeHubs) {
                String hubInfo = String.format("%s (Вільних місць: %d/%d)",
                        hub.getTitle(), hub.getSlotsAvailable(), hub.getSlotsTotal());
                hubsListView.getItems().add(hubInfo);
            }
        }

        // 4. Обробка подвійного кліку по списку хабів
        hubsListView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                int selectedIndex = hubsListView.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0 && !activeHubs.isEmpty()) {
                    Hub selectedHub = activeHubs.get(selectedIndex);
                    openHubDetails(event, selectedHub);
                }
            }
        });

        // 5. Запускаємо ініціалізацію офлайн-карти та малювання кіл хабів
        initOfflineMap();
    }

    private void initOfflineMap() {
        // Створюємо міст між JavaFX та Swing
        SwingNode swingNode = new SwingNode();
        mapContainer.getChildren().add(swingNode);

        // Весь код Swing МАЄ виконуватися в окремому потоці
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Створюємо саму карту
                mapView = new MapView();
                mapView.getMapScaleBar().setVisible(true);

                // 2. Використовуємо кеш у пам'яті (вирішує проблему з AwtUtil)
                TileCache tileCache = new org.mapsforge.map.layer.cache.InMemoryTileCache(500);

                // 3. Шукаємо наш файл
                File mapFile = new File("ukraine.map");
                if (!mapFile.exists()) {
                    System.err.println("ПОМИЛКА: Файл ukraine.map не знайдено в кореневій папці проєкту!");
                    return; // Зупиняємо завантаження карти, якщо файлу немає
                }

                MapDataStore mapDataStore = new MapFile(mapFile);

                // 4. Налаштовуємо шар рендерингу (малювання вулиць і будинків)
                TileRendererLayer tileRendererLayer = new TileRendererLayer(
                        tileCache,
                        mapDataStore,
                        mapView.getModel().mapViewPosition,
                        AwtGraphicFactory.INSTANCE
                );

                // 5. Встановлюємо стандартну тему OSM
                tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
                mapView.getLayerManager().getLayers().add(tileRendererLayer);

                // 6. Центруємо карту по Києву та задаємо зум
                mapView.getModel().mapViewPosition.setCenter(new LatLong(50.4501, 30.5234));
                mapView.getModel().mapViewPosition.setZoomLevel((byte) 12);

                // 7. ОБГОРТКА: кладемо карту в JPanel, щоб SwingNode її прийняв
                javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout());
                panel.add(mapView);

                // 8. Вставляємо готовий Swing-компонент у JavaFX
                Platform.runLater(() -> swingNode.setContent(panel));

                drawHubsOnMap(hubService.getAllActiveHubs());

            } catch (Exception e) {
                System.err.println("Помилка ініціалізації Mapsforge: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Відкриває деталі і передає туди дані хабу
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
        boolean isSelected = roleToggleButton.isSelected(); // true, якщо перемкнули на Хоста

        if (isSelected) {
            roleToggleButton.setText("Режим хоста");
        } else {
            roleToggleButton.setText("Режим гостя");
        }

        // Динамічно ховаємо/показуємо кнопку при натисканні
        if (createHubButton != null) {
            createHubButton.setVisible(isSelected);
            createHubButton.setManaged(isSelected);
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

    // Малювання хабів поверх офлайн-карти
    private void drawHubsOnMap(List<Hub> activeHubs) {
        // Створюємо стиль для заливки (Напівпрозорий червоний)
        Paint fillPaint = AwtGraphicFactory.INSTANCE.createPaint();
        fillPaint.setColor(AwtGraphicFactory.INSTANCE.createColor(150, 255, 0, 0));
        fillPaint.setStyle(Style.FILL);

        // Створюємо стиль для контуру (Чорний)
        Paint strokePaint = AwtGraphicFactory.INSTANCE.createPaint();
        strokePaint.setColor(AwtGraphicFactory.INSTANCE.createColor(255, 0, 0, 0));
        strokePaint.setStrokeWidth(2);
        strokePaint.setStyle(Style.STROKE);

        // Проходимось по всіх хабах і малюємо коло на їх координатах
        for (Hub hub : activeHubs) {
            LatLong latLong = new LatLong(hub.getLatitude(), hub.getLongitude());

            // 150 - це радіус кола в метрах
            Circle circle = new Circle(latLong, 150, fillPaint, strokePaint);

            // Додаємо коло на шар карти
            mapView.getLayerManager().getLayers().add(circle);
        }
    }
}