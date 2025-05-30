package com.auction.controller;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.auction.exception.ServiceException;
import com.auction.model.Log;
import com.auction.model.User;
import com.auction.service.AuctionService;
import com.auction.service.LogService;
import com.auction.service.UserService;
import com.auction.util.Navigator;
import com.auction.util.ValidationUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class ProfileController implements Initializable {
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label balanceLabel;
    @FXML private Label messageLabel;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    
    // Компоненти для таблиці логів
    @FXML private TableView<Log> userLogsTable;
    @FXML private TableColumn<Log, String> logActionColumn;
    @FXML private TableColumn<Log, String> logDateColumn;
    
    // Компоненти для статистики аукціонів
    @FXML private Label participatedAuctionsLabel;
    @FXML private Label wonAuctionsLabel;
    @FXML private Label winPercentageLabel;
    
    // Форматування дати і часу
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    
    private final UserService userService = new UserService();
    private final LogService logService = new LogService();
    private final AuctionService auctionService = new AuctionService();
    private User currentUser;
    
    
    private ObservableList<Log> userLogs = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshUserData();
        loadUserData();
        setupLogColumns();
        loadUserLogs();
        loadAuctionStatistics();
    }

     //Завантажує статистику аукціонів користувача
    private void loadAuctionStatistics() {
        System.out.println("Завантаження статистики аукціонів...");
        try {
            if (currentUser == null) {
                System.out.println("Помилка: currentUser є null");
                return;
            }
            
            Long userId = currentUser.getId();
            System.out.println("Отримано userId: " + userId);
            
            // Перевіряємо, чи існують компоненти інтерфейсу
            if (participatedAuctionsLabel == null || wonAuctionsLabel == null || 
                winPercentageLabel == null) {
                System.out.println("Помилка: один або більше компонентів інтерфейсу є null");
                return;
            }
            
            // Отримуємо статистику
            System.out.println("Отримуємо кількість аукціонів, в яких користувач брав участь...");
            int participated = auctionService.getParticipatedAuctionsCount(userId);
            System.out.println("Кількість аукціонів, в яких користувач брав участь: " + participated);
            
            System.out.println("Отримуємо кількість виграних аукціонів...");
            int won = auctionService.getWonAuctionsCount(userId);
            System.out.println("Кількість виграних аукціонів: " + won);
            
            System.out.println("Отримуємо відсоток виграних аукціонів...");
            double winPercentage = auctionService.getWinPercentage(userId);
            System.out.println("Відсоток виграних аукціонів: " + winPercentage);
            
            // Відображаємо статистику
            System.out.println("Встановлюємо текст для компонентів інтерфейсу...");
            participatedAuctionsLabel.setText(String.valueOf(participated));
            wonAuctionsLabel.setText(String.valueOf(won));
            winPercentageLabel.setText(String.format("%.1f%%", winPercentage));
            System.out.println("Статистика аукціонів успішно завантажена");
        } catch (Exception e) {
            System.out.println("Помилка при завантаженні статистики аукціонів: " + e.getMessage());
            e.printStackTrace();
            showError("Помилка при завантаженні статистики аукціонів: " + e.getMessage());
        }
    }
    
    private void refreshUserData() {
        
        Navigator.refreshCurrentUser();
        
        currentUser = Navigator.getCurrentUser();
    }
    
    private void loadUserData() {
        usernameLabel.setText(currentUser.getUsername());
        emailLabel.setText(currentUser.getEmail());
        roleLabel.setText(currentUser.getRole());
        balanceLabel.setText(String.format("%.2f грн", currentUser.getBalance()));
    }
    
        private void setupLogColumns() {
        // Налаштування колонки дії
        logActionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getAction()));
        
        // Налаштування колонки дати та часу
        logDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime timestamp = cellData.getValue().getCreatedAt();
            return new SimpleStringProperty(timestamp.format(DATE_TIME_FORMATTER));
        });
    }
    
        private void loadUserLogs() {
        try {
            // Отримуємо логи тільки для поточного користувача
            List<Log> logs = logService.findByUser(currentUser.getId());
            userLogs.setAll(logs);
            userLogsTable.setItems(userLogs);
            
            showMessage("Завантажено " + logs.size() + " записів історії дій");
        } catch (Exception e) {
            showError("Помилка при завантаженні історії дій: " + e.getMessage());
        }
    }
    
        @FXML
    private void handleRefreshLogs() {
        refreshUserData();
        loadUserLogs();
        loadAuctionStatistics();
        showMessage("Інформацію оновлено");
    }
    
    @FXML
    private void handleAddBalance() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Поповнення балансу");
        dialog.setHeaderText(null);
        dialog.setContentText("Введіть суму поповнення:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amount -> {
            try {
                double value = Double.parseDouble(amount);
                if (value <= 0) {
                    showError("Сума поповнення повинна бути більше 0");
                    return;
                }
                userService.updateBalance(currentUser.getId(), value);
                currentUser = userService.findById(currentUser.getId());
                loadUserData();
                showSuccess("Баланс успішно поповнено");
            } catch (NumberFormatException e) {
                showError("Некоректна сума");
            } catch (ServiceException e) {
                showError(e.getMessage());
            }
        });
    }
    
    @FXML
    private void handleChangePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Будь ласка, заповніть всі поля");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showError("Паролі не співпадають");
            return;
        }
        
        String validationError = ValidationUtils.validatePassword(newPassword);
        if (validationError != null) {
            showError(validationError);
            return;
        }
        
        try {
            userService.changePassword(currentUser.getId(), oldPassword, newPassword);
            showSuccess("Пароль успішно змінено");
            clearPasswordFields();
        } catch (ServiceException e) {
            showError(e.getMessage());
        }
    }
    
    @FXML
    private void handleBack() {
        Stage stage = (Stage) usernameLabel.getScene().getWindow();
        Navigator.navigateToMain(stage);
    }
    
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: red;");
    }
    
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: green;");
    }
    
    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: black;");
    }
    
    private void clearPasswordFields() {
        oldPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
} 