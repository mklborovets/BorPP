package com.auction.controller;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.auction.model.Auction;
import com.auction.model.Log;
import com.auction.model.User;
import com.auction.model.Vehicle;
import com.auction.service.AuctionService;
import com.auction.service.LogService;
import com.auction.service.UserService;
import com.auction.service.VehicleService;
import com.auction.util.Navigator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Контролер для панелі адміністратора
 * Спрощена версія, яка відображає тільки логи системи
 */
public class AdminDashboardController implements Initializable {

    // Логер для відстеження роботи контролера
    private static final Logger logger = Logger.getLogger(AdminDashboardController.class.getName());
    
    // Сервіси для роботи з даними
    private final LogService logService = new LogService();
    private final UserService userService = new UserService();
    private final AuctionService auctionService = new AuctionService();
    private final VehicleService vehicleService = new VehicleService();
    
    // Форматування дати і часу
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    
    // Колекції даних
    private ObservableList<Log> logs = FXCollections.observableArrayList();
    private FilteredList<Log> filteredLogs;
    
    // Колекції даних для користувачів
    private ObservableList<User> users = FXCollections.observableArrayList();
    private FilteredList<User> filteredUsers;
    
    // Колекції даних для аукціонів
    private ObservableList<Auction> auctions = FXCollections.observableArrayList();
    private FilteredList<Auction> filteredAuctions;
    
    // Компоненти інтерфейсу для логів
    @FXML private TableView<Log> logsTable;
    @FXML private TableColumn<Log, Long> logIdColumn;
    @FXML private TableColumn<Log, String> logDateColumn;
    @FXML private TableColumn<Log, String> logUserColumn;
    @FXML private TableColumn<Log, String> logActionColumn;
    @FXML private TextField actionFilterField;
    
    // Компоненти інтерфейсу для управління користувачами
    @FXML private VBox logsPane;
    @FXML private VBox usersPane;
    @FXML private VBox auctionsPane;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Long> userIdColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Double> balanceColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private TextField userSearchField;
    
    // Компоненти інтерфейсу для управління аукціонами
    @FXML private TableView<Auction> auctionsTable;
    @FXML private TableColumn<Auction, Long> auctionIdColumn;
    @FXML private TableColumn<Auction, String> auctionVehicleColumn;
    @FXML private TableColumn<Auction, String> auctionSellerColumn;
    @FXML private TableColumn<Auction, Double> auctionStartPriceColumn;
    @FXML private TableColumn<Auction, Double> auctionCurrentPriceColumn;
    @FXML private TableColumn<Auction, String> auctionStatusColumn;
    @FXML private TableColumn<Auction, Void> auctionActionsColumn;
    @FXML private TextField auctionSearchField;
    
    // Загальні компоненти інтерфейсу
    @FXML private Label adminNameLabel;
    @FXML private ComboBox<String> userFilterComboBox;
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    
    // Поточний користувач-адміністратор
    private User currentAdmin;
    
    /**
     * Ініціалізація контролера
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing admin dashboard...");
        
        // Отримуємо поточного користувача-адміністратора
        List<User> admins = userService.findAll().stream()
                .filter(u -> "admin".equals(u.getRole()))
                .collect(Collectors.toList());
        currentAdmin = admins.isEmpty() ? new User("admin", "", "admin@example.com") : admins.get(0);
        adminNameLabel.setText(currentAdmin.getUsername());
        
        // Налаштовуємо колонки таблиці логів
        setupLogColumns();
        
        // Налаштовуємо колонки таблиці користувачів
        setupUserColumns();
        
        // Налаштовуємо фільтри
        setupFilters();
        
        // Прив'язуємо дані до таблиці логів
        filteredLogs = new FilteredList<>(logs, p -> true);
        logsTable.setItems(filteredLogs);
        
        // Прив'язуємо дані до таблиці користувачів
        filteredUsers = new FilteredList<>(users, p -> true);
        usersTable.setItems(filteredUsers);
        
        // Завантажуємо дані логів один раз при ініціалізації
        loadInitialData();
        
        // За замовчуванням показуємо панель логів
        showLogsPane();
    }
    
    //Завантажує початкові дані при ініціалізації
    private void loadInitialData() {
        logger.info("Loading initial data...");
        long startTime = System.currentTimeMillis();
        
        try {
            List<Log> logsList = logService.findAll();
            logs.setAll(logsList);
            logger.info("Loaded " + logsList.size() + " logs from database");
            
            List<User> usersList = userService.findAll();
            users.setAll(usersList);
            logger.info("Loaded " + usersList.size() + " users from database");
            
            populateUserFilter();
            
            loadAuctions();
            
            long endTime = System.currentTimeMillis();
            logger.info("Initial data loading completed in " + (endTime - startTime) + "ms");
        } catch (Exception e) {
            logger.severe("Error loading initial data: " + e.getMessage());
        }
    }
    
    //Завантажує аукціони з бази даних
    private void loadAuctions() {
        logger.info("Loading auctions...");
        try {
            List<Auction> auctionsList = auctionService.findAll();
            
            for (Auction auction : auctionsList) {
                try {
                    Vehicle vehicle = vehicleService.findById(auction.getVehicleId());
                    if (vehicle != null) {
                        auction.setVehicleInfo(vehicle.getBrand() + " " + vehicle.getModel());
                    } else {
                        auction.setVehicleInfo("Транспорт не знайдено");
                    }
                } catch (Exception e) {
                    auction.setVehicleInfo("Транспорт не знайдено");
                }
                
                try {
                    User seller = userService.findById(auction.getUserId());
                    if (seller != null) {
                        auction.setSellerUsername(seller.getUsername());
                    } else {
                        auction.setSellerUsername("Продавець не знайдений");
                    }
                } catch (Exception e) {
                    auction.setSellerUsername("Продавець не знайдений");
                }
            }
            
            auctions.setAll(auctionsList);
            logger.info("Loaded " + auctionsList.size() + " auctions from database");
            
            setupAuctionColumns();
            
            filteredAuctions = new FilteredList<>(auctions, p -> true);
            auctionsTable.setItems(filteredAuctions);
        } catch (Exception e) {
            logger.severe("Error loading auctions: " + e.getMessage());
        }
    }
    
    /**
     * Заповнює список користувачів для фільтрації
     */
    private void populateUserFilter() {
        try {
            // Отримуємо унікальних користувачів з логів
            Set<Long> userIds = logs.stream()
                .map(Log::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            
            ObservableList<String> userOptions = FXCollections.observableArrayList("Система");
            // Додаємо користувачів з бази даних
            for (Long userId : userIds) {
                try {
                    User user = userService.findById(userId);
                    if (user != null) {
                        userOptions.add("Користувач #" + userId);
                    }
                } catch (Exception e) {
                    userOptions.add("Користувач #" + userId);
                }
            }

            userFilterComboBox.setItems(userOptions);
            logger.info("Populated user filter with " + userOptions.size() + " options");
        } catch (Exception e) {
            logger.warning("Error populating user filter: " + e.getMessage());
        }
    }
    
    /**
     * Налаштування колонок для таблиці логів
     */
    private void setupLogColumns() {
        logger.info("Setting up log columns...");
        logIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        logDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime timestamp = cellData.getValue().getCreatedAt();
            return new SimpleStringProperty(timestamp.format(DATE_TIME_FORMATTER));
        });
        
        logUserColumn.setCellValueFactory(cellData -> {
            Long userId = cellData.getValue().getUserId();
            if (userId == null) {
                return new SimpleStringProperty("Система");
            } else {
                return new SimpleStringProperty("Користувач #" + userId);
            }
        });
        
        logActionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
    }
    
    /**
     * Налаштування колонок для таблиці користувачів
     */
    private void setupUserColumns() {
        logger.info("Setting up user columns...");
        
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
        
        actionsColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button deleteButton = new Button("Видалити");
            private final Button adminButton = new Button("Зробити адміном");
            private final Button removeAdminButton = new Button("Забрати права адміна");
            private final HBox buttonsBox = new HBox(5); // 5 - це відстань між кнопками
            
            {
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    AdminDashboardController.this.handleDeleteUser(user);
                });
                
                adminButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    AdminDashboardController.this.handleMakeAdmin(user);
                });
                
                removeAdminButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    AdminDashboardController.this.handleRemoveAdmin(user);
                });
                
                deleteButton.getStyleClass().add("button-danger");
                adminButton.getStyleClass().add("button-admin");
                removeAdminButton.getStyleClass().add("button-secondary");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    buttonsBox.getChildren().clear();
                    
                    // Не дозволяємо видаляти поточного адміністратора
                    if (!user.getId().equals(currentAdmin.getId())) {
                        buttonsBox.getChildren().add(deleteButton);
                        
                        // Показуємо кнопку "Зробити адміном" або "Забрати права адміна" залежно від ролі
                        if ("admin".equalsIgnoreCase(user.getRole())) {
                            buttonsBox.getChildren().add(removeAdminButton);
                        } else {
                            buttonsBox.getChildren().add(adminButton);
                        }
                        
                        setGraphic(buttonsBox);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }
    
    /**
     * Налаштування колонок для таблиці аукціонів
     */
    private void setupAuctionColumns() {
        logger.info("Setting up auction columns...");
        
        auctionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        auctionVehicleColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleInfo"));

        auctionSellerColumn.setCellValueFactory(new PropertyValueFactory<>("sellerUsername"));
        
        auctionStartPriceColumn.setCellValueFactory(new PropertyValueFactory<>("startPrice"));
        auctionCurrentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        auctionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        auctionActionsColumn.setCellFactory(param -> new TableCell<Auction, Void>() {
            private final Button deleteButton = new Button("Видалити");
            
            {
                deleteButton.setOnAction(event -> {
                    Auction auction = getTableView().getItems().get(getIndex());
                    AdminDashboardController.this.handleDeleteAuction(auction);
                });
                
                deleteButton.getStyleClass().add("button-danger");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
    }
    
    /**
     * Налаштування фільтрів для логів
     */
    private void setupFilters() {
        logger.info("Setting up filters...");
        
        // Налаштовуємо обробник для фільтра за текстом дії
        actionFilterField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyLogFilter(newValue);
        });
        
        // Налаштовуємо обробник для фільтра за користувачем
        userFilterComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            applyLogFilter(actionFilterField.getText());
        });
        
        // Налаштовуємо обробники для фільтрів за датою
        dateFromPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyLogFilter(actionFilterField.getText());
        });
        
        dateToPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyLogFilter(actionFilterField.getText());
        });
    }
    
    /**
     * Показує панель логів
     */
    private void showLogsPane() {
        logsPane.setVisible(true);
        logsPane.setManaged(true);
        usersPane.setVisible(false);
        usersPane.setManaged(false);
        auctionsPane.setVisible(false);
        auctionsPane.setManaged(false);
    }
    
    /**
     * Показує панель управління користувачами
     */
    private void showUsersPane() {
        logsPane.setVisible(false);
        logsPane.setManaged(false);
        usersPane.setVisible(true);
        usersPane.setManaged(true);
        auctionsPane.setVisible(false);
        auctionsPane.setManaged(false);
    }
    
    /**
     * Показує панель управління аукціонами
     */
    private void showAuctionsPane() {
        logsPane.setVisible(false);
        logsPane.setManaged(false);
        usersPane.setVisible(false);
        usersPane.setManaged(false);
        auctionsPane.setVisible(true);
        auctionsPane.setManaged(true);
    }
    
    /**
     * Обробник для кнопки показу логів
     */
    @FXML
    private void handleShowLogs() {
        logger.info("Showing logs pane...");
        showLogsPane();
    }
    
    /**
     * Обробник для кнопки показу користувачів
     */
    @FXML
    private void handleShowUsers() {
        logger.info("Showing users pane...");
        showUsersPane();
    }
    
    /**
     * Обробник для кнопки показу аукціонів
     */
    @FXML
    private void handleShowAuctions() {
        logger.info("Showing auctions pane...");
        showAuctionsPane();
        loadAuctions();
    }
    
    /**
     * Обробник для пошуку користувачів
     */
    @FXML
    private void handleUserSearch() {
        String searchText = userSearchField.getText().toLowerCase();
        logger.info("Searching users with text: " + searchText);
        
        filteredUsers.setPredicate(user -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            
            return user.getUsername().toLowerCase().contains(searchText) ||
                   user.getEmail().toLowerCase().contains(searchText) ||
                   user.getRole().toLowerCase().contains(searchText);
        });
    }
    
    /**
     * Обробник для скидання пошуку користувачів
     */
    @FXML
    private void handleResetUserSearch() {
        logger.info("Resetting user search...");
        userSearchField.clear();
        filteredUsers.setPredicate(user -> true);
    }
    
    /**
     * Обробник для пошуку аукціонів
     */
    @FXML
    private void handleAuctionSearch() {
        String searchText = auctionSearchField.getText().toLowerCase();
        logger.info("Searching auctions with text: " + searchText);
        
        filteredAuctions.setPredicate(auction -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }

            if (auction.getId().toString().toLowerCase().contains(searchText)) {
                return true;
            }
 
            if (auction.getStatus().toLowerCase().contains(searchText)) {
                return true;
            }

            try {
                Vehicle vehicle = vehicleService.findById(auction.getVehicleId());
                if (vehicle != null) {
                    String vehicleInfo = vehicle.getBrand() + " " + vehicle.getModel();
                    if (vehicleInfo.toLowerCase().contains(searchText)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                
            }
 
            try {
                User seller = userService.findById(auction.getUserId());
                if (seller != null && seller.getUsername().toLowerCase().contains(searchText)) {
                    return true;
                }
            } catch (Exception e) {
                // Ігноруємо помилку
            }
            
            return false;
        });
    }
    
    //Обробник для скидання пошуку аукціонів
    @FXML
    private void handleResetAuctionSearch() {
        logger.info("Resetting auction search...");
        auctionSearchField.clear();
        filteredAuctions.setPredicate(auction -> true);
    }
    
    //Обробник для видалення користувача
     
    private void handleDeleteUser(User user) {
        if (user.getId().equals(currentAdmin.getId())) {
            logger.warning("Cannot delete current admin user");
            return;
        }
        
        logger.info("Attempting to delete user: " + user.getUsername());
        
        // Показуємо діалог підтвердження
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження видалення");
        alert.setHeaderText("Видалення користувача");
        alert.setContentText("Ви впевнені, що хочете видалити користувача " + user.getUsername() + "?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                logService.createLog(currentAdmin.getId(), "Видалено користувача: " + user.getUsername());
                
                userService.delete(user.getId());
                
                users.remove(user);
                
                logger.info("User deleted successfully: " + user.getUsername());
            } catch (Exception e) {
                logger.severe("Error deleting user: " + e.getMessage());

                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Помилка");
                errorAlert.setHeaderText("Помилка видалення користувача");
                errorAlert.setContentText("Не вдалося видалити користувача: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }
    
    //Обробник для видалення аукціону
    private void handleDeleteAuction(Auction auction) {
        logger.info("Attempting to delete auction: #" + auction.getId());
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження видалення");
        alert.setHeaderText("Видалення аукціону");
        alert.setContentText("Ви впевнені, що хочете видалити аукціон #" + auction.getId() + "? Всі ставки на цей аукціон також будуть видалені.");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Видаляємо аукціон разом зі ставками
                auctionService.deleteAuctionWithBids(auction.getId(), currentAdmin.getId());
                
                auctions.remove(auction);
                
                logger.info("Auction deleted successfully: #" + auction.getId());
            } catch (Exception e) {
                logger.severe("Error deleting auction: " + e.getMessage());
                
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Помилка");
                errorAlert.setHeaderText("Помилка видалення аукціону");
                errorAlert.setContentText("Не вдалося видалити аукціон: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }
    
    //Застосовує фільтр до таблиці логів
     
    private void applyLogFilter(String filterText) {
        String searchText = filterText;
        String userFilter = userFilterComboBox.getValue();
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;

        if (dateFromPicker.getValue() != null) {
            fromDate = dateFromPicker.getValue().atStartOfDay();
        }
        
        if (dateToPicker.getValue() != null) {
            toDate = dateToPicker.getValue().atTime(23, 59, 59);
        }

        final LocalDateTime finalFromDate = fromDate;
        final LocalDateTime finalToDate = toDate;
        final String finalUserFilter = userFilter;

        filteredLogs.setPredicate(log -> {
            boolean matchesText = true;
            boolean matchesDateRange = true;
            boolean matchesUser = true;

            if (!searchText.isEmpty()) {
                matchesText = log.getId().toString().toLowerCase().contains(searchText) ||
                              log.getAction().toLowerCase().contains(searchText) ||
                              log.getCreatedAt().toString().toLowerCase().contains(searchText);
            }

            if (finalFromDate != null) {
                matchesDateRange = !log.getCreatedAt().isBefore(finalFromDate);
            }
            
            if (finalToDate != null) {
                matchesDateRange = matchesDateRange && !log.getCreatedAt().isAfter(finalToDate);
            }

            if (finalUserFilter != null && !finalUserFilter.isEmpty()) {
                Long userId = log.getUserId();
                if (userId == null) {
                    matchesUser = finalUserFilter.equals("Система");
                } else {
                    matchesUser = finalUserFilter.equals("Користувач #" + userId);
                }
            }
            
            return matchesText && matchesDateRange && matchesUser;
        });
        
        logger.info("Filter applied - Text: '" + searchText + "', Date range: " + 
                  (fromDate != null ? fromDate : "any") + " - " + 
                  (toDate != null ? toDate : "any") + ", User: " + 
                  (userFilter != null ? userFilter : "any"));
    }
    
    //Обробник для кнопки застосування фільтра
     
    @FXML
    private void handleApplyFilter() {
        logger.info("Applying filter...");
        applyLogFilter(actionFilterField.getText());
    }
    
    //Обробник для кнопки скидання фільтра
    @FXML
    private void handleResetFilter() {
        logger.info("Resetting filter...");
        actionFilterField.clear();
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        userFilterComboBox.getSelectionModel().clearSelection();

        filteredLogs.setPredicate(log -> true);
    }
    
    @FXML
    private void handleRefreshAllData() {
        logger.info("Refreshing all data...");
        loadInitialData();
    }
    
    @FXML
    private void handleRefreshLogs() {
        logger.info("Refreshing logs...");
        try {
            List<Log> logsList = logService.findAll();
            logs.setAll(logsList);
            logger.info("Refreshed " + logsList.size() + " logs");
        } catch (Exception e) {
            logger.severe("Error refreshing logs: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleBack() {
        logger.info("Navigating back to main page...");
        Stage stage = (Stage) adminNameLabel.getScene().getWindow();
        Navigator.navigateToMain(stage);
    }
    
    /**
     * Обробник для надання користувачу прав адміністратора
     */
    private void handleMakeAdmin(User user) {
        logger.info("Attempting to make user admin: " + user.getUsername());
        
        // Показуємо діалог підтвердження
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження дії");
        alert.setHeaderText("Надання прав адміністратора");
        alert.setContentText("Ви впевнені, що хочете надати права адміністратора користувачу " + user.getUsername() + "?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                userService.changeRole(user.getId(), "admin");
                
                // Оновлюємо роль користувача в інтерфейсі
                user.setRole("admin");
                
                // Оновлюємо таблицю
                usersTable.refresh();
                
                // Логуємо дію
                logService.createLog(currentAdmin.getId(), "Надано права адміністратора користувачу: " + user.getUsername());
                
                logger.info("User " + user.getUsername() + " is now an admin");
                
                // Показуємо повідомлення про успіх
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Успіх");
                successAlert.setHeaderText("Права адміністратора надано");
                successAlert.setContentText("Користувач " + user.getUsername() + " тепер має права адміністратора.");
                successAlert.showAndWait();
            } catch (Exception e) {
                logger.severe("Error making user admin: " + e.getMessage());

                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Помилка");
                errorAlert.setHeaderText("Помилка надання прав адміністратора");
                errorAlert.setContentText("Не вдалося надати права адміністратора: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }
    
    /**
     * Обробник для видалення прав адміністратора у користувача
     */
    private void handleRemoveAdmin(User user) {
        logger.info("Attempting to remove admin rights from user: " + user.getUsername());
        
        // Показуємо діалог підтвердження
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Підтвердження дії");
        alert.setHeaderText("Видалення прав адміністратора");
        alert.setContentText("Ви впевнені, що хочете видалити права адміністратора у користувача " + user.getUsername() + "?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                userService.changeRole(user.getId(), "user");
                
                // Оновлюємо роль користувача в інтерфейсі
                user.setRole("user");
                
                // Оновлюємо таблицю
                usersTable.refresh();
                
                // Логуємо дію
                logService.createLog(currentAdmin.getId(), "Видалено права адміністратора у користувача: " + user.getUsername());
                
                logger.info("Admin rights removed from user " + user.getUsername());
                
                // Показуємо повідомлення про успіх
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Успіх");
                successAlert.setHeaderText("Права адміністратора видалено");
                successAlert.setContentText("Користувач " + user.getUsername() + " більше не має прав адміністратора.");
                successAlert.showAndWait();
            } catch (Exception e) {
                logger.severe("Error removing admin rights: " + e.getMessage());

                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Помилка");
                errorAlert.setHeaderText("Помилка видалення прав адміністратора");
                errorAlert.setContentText("Не вдалося видалити права адміністратора: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }
}
