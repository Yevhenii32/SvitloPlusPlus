package com.noideasolutions.svitlo.controller;

import com.noideasolutions.svitlo.model.PartnerReward;
import com.noideasolutions.svitlo.model.User;
import com.noideasolutions.svitlo.service.BonusService;
import com.noideasolutions.svitlo.service.UserSession;
import com.noideasolutions.svitlo.util.SceneSwitcher;
import com.noideasolutions.svitlo.exception.SvitloException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class UserProfileController {

    @FXML
    private Label usernameLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label bonusPointsLabel;

    @FXML
    private ListView<PartnerReward> rewardsListView;

    private BonusService bonusService;
    private User currentUser;

    @FXML
    public void initialize() {
        this.bonusService = new BonusService();
        this.currentUser = UserSession.getInstance().getCurrentUser();

        // Завантажуємо дані користувача на екран
        updateUserInfo();

        // Налаштовуємо красиве відображення списку винагород
        rewardsListView.setCellFactory(param -> new ListCell<PartnerReward>() {
            @Override
            protected void updateItem(PartnerReward item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle() + " від " + item.getPartnerName() + " — Ціна: " + item.getCostInPoints() + " балів");
                }
            }
        });

        // Завантажуємо тестові знижки (Mock)
        loadMockRewards();
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            usernameLabel.setText("Користувач: " + currentUser.getUsername());
            roleLabel.setText("Роль: " + currentUser.getRole());
            bonusPointsLabel.setText(currentUser.getBonusPoints() + " балів");
        }
    }

    private void loadMockRewards() {
        // Оскільки у нас за ТЗ симуляція, просто генеруємо їх у пам'яті
        rewardsListView.getItems().add(new PartnerReward(1, "Безкоштовна кава", "Aroma Kava", 150));
        rewardsListView.getItems().add(new PartnerReward(2, "Знижка 10% на павербанк", "Rozetka", 500));
        rewardsListView.getItems().add(new PartnerReward(3, "Промокод 100 грн на таксі", "Uklon", 300));
    }

    @FXML
    private void handleRedeemAction(ActionEvent event) {
        PartnerReward selectedReward = rewardsListView.getSelectionModel().getSelectedItem();

        if (selectedReward == null) {
            showAlert(Alert.AlertType.WARNING, "Увага", "Будь ласка, оберіть винагороду зі списку!");
            return;
        }

        try {
            // Викликаємо твій готовий метод з BonusService
            boolean success = bonusService.redeemReward(currentUser, selectedReward);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Успіх!",
                        "Ви успішно обміняли бали на: " + selectedReward.getTitle());
                // Оновлюємо баланс на екрані
                updateUserInfo();
            } else {
                showAlert(Alert.AlertType.ERROR, "Недостатньо балів",
                        "Вам не вистачає балів для цієї винагороди.");
            }
        } catch (SvitloException e) {
            showAlert(Alert.AlertType.ERROR, "Помилка бази даних", e.getMessage());
        }
    }

    @FXML
    private void handleBackAction(ActionEvent event) {
        SceneSwitcher.switchTo(event, "/com/noideasolutions/svitlo/controller/MainDashboard.fxml", "Головне меню");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}