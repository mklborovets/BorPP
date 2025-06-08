package com.auction.controller;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import com.auction.model.Auction;
import com.auction.model.Vehicle;
import com.auction.service.AuctionService;
import com.auction.service.BidService;
import com.auction.service.UserService;
import com.auction.service.VehicleService;
import com.auction.util.Navigator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MainController implements Initializable {
    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterBox;
    @FXML private TableView<Auction> auctionsTable;
    
    
    @FXML private ComboBox<String> brandFilterBox;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private TextField minMileageField;
    @FXML private TextField maxMileageField;
    @FXML private TableColumn<Auction, String> vehicleColumn;
    @FXML private TableColumn<Auction, Double> startPriceColumn;
    @FXML private TableColumn<Auction, Double> currentPriceColumn;
    @FXML private TableColumn<Auction, Double> priceStepColumn;
    @FXML private TableColumn<Auction, String> startTimeColumn;
    @FXML private TableColumn<Auction, String> endTimeColumn;
    @FXML private TableColumn<Auction, String> statusColumn;
    @FXML private TableColumn<Auction, Integer> bidsCountColumn;
    @FXML private TableColumn<Auction, Void> actionsColumn;
    @FXML private Label totalAuctionsLabel;
    @FXML private Label activeAuctionsLabel;
    @FXML private Label userLabel;
    @FXML private Button adminDashboardButton;

    private final AuctionService auctionService = new AuctionService();
    private final VehicleService vehicleService = new VehicleService();
    private final UserService userService = new UserService();
    private final BidService bidService = new BidService();
    private ObservableList<Auction> auctions;
    private FilteredList<Auction> filteredAuctions;
    private Timer updateTimer;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        setupFilters();
        loadAuctions();
        
        
        welcomeLabel.setText("Вітаємо, " + Navigator.getCurrentUser().getUsername() + "!");
        userLabel.setText(Navigator.getCurrentUser().getUsername());
        
        
        if (userService.isAdmin(Navigator.getCurrentUser().getId())) {
            
            if (adminDashboardButton != null) {
                adminDashboardButton.setVisible(true);
            }
        } else {
            
            if (adminDashboardButton != null) {
                adminDashboardButton.setVisible(false);
            }
        }
        
        startUpdateTimer();
    }

    private void setupFilters() {
        
        filterBox.setItems(FXCollections.observableArrayList(
            "Всі", "Активні", "Завершені", "Мої аукціони"
        ));
        filterBox.setValue("Всі");
        
        // Налаштовуємо фільтр за брендом
        if (brandFilterBox != null) {
            // Отримуємо унікальні бренди з бази даних
            List<String> brands = vehicleService.getAllBrands();
            ObservableList<String> brandItems = FXCollections.observableArrayList();
            brandItems.add("Всі бренди");
            brandItems.addAll(brands);
            brandFilterBox.setItems(brandItems);
            brandFilterBox.setValue("Всі бренди");
        }
        
        // Додаємо обробники подій для полів фільтрації
        if (minPriceField != null && maxPriceField != null && minMileageField != null && maxMileageField != null) {
            
            minPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    minPriceField.setText(newValue.replaceAll("[^\\d]", ""));
                }
                applyFilters();
            });
            
            maxPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    maxPriceField.setText(newValue.replaceAll("[^\\d]", ""));
                }
                applyFilters();
            });
            
            minMileageField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    minMileageField.setText(newValue.replaceAll("[^\\d]", ""));
                }
                applyFilters();
            });
            
            maxMileageField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    maxMileageField.setText(newValue.replaceAll("[^\\d]", ""));
                }
                applyFilters();
            });
        }
        
        // Додаємо обробник подій для фільтра за брендом
        if (brandFilterBox != null) {
            brandFilterBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                applyFilters();
            });
        }
        
        // Налаштовуємо пошук та фільтрацію
        filterBox.setOnAction(e -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupColumns() {
        // Налаштовуємо колонки таблиці
        vehicleColumn.setCellValueFactory(data -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> data.getValue().getVehicleInfo()
            )
        );
        
        startPriceColumn.setCellValueFactory(
            new PropertyValueFactory<>("startPrice")
        );
        startPriceColumn.setCellFactory(col -> new TableCell<Auction, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f $", price));
                }
            }
        });
        
        currentPriceColumn.setCellValueFactory(
            new PropertyValueFactory<>("currentPrice")
        );
        currentPriceColumn.setCellFactory(col -> new TableCell<Auction, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f $", price));
                }
            }
        });
        
        
        priceStepColumn.setCellValueFactory(
            new PropertyValueFactory<>("priceStep")
        );
        priceStepColumn.setCellFactory(col -> new TableCell<Auction, Double>() {
            @Override
            protected void updateItem(Double step, boolean empty) {
                super.updateItem(step, empty);
                if (empty || step == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f $", step));
                }
            }
        });
        
        
        bidsCountColumn.setCellValueFactory(
            new PropertyValueFactory<>("bidCount")
        );
        
        
        startTimeColumn.setCellValueFactory(data ->
            javafx.beans.binding.Bindings.createStringBinding(
                () -> {
                    Auction auction = data.getValue();
                    return auction.getStartTime().format(DATE_TIME_FORMATTER);
                }
            )
        );
        
        
        endTimeColumn.setCellValueFactory(data ->
            javafx.beans.binding.Bindings.createStringBinding(
                () -> {
                    Auction auction = data.getValue();
                    return auction.getEndTime().format(DATE_TIME_FORMATTER);
                }
            )
        );
        
        statusColumn.setCellValueFactory(
            new PropertyValueFactory<>("status")
        );
        statusColumn.setCellFactory(col -> new TableCell<Auction, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                } else {
                    switch (status) {
                        case "ACTIVE" -> setText("Активний");
                        case "PENDING" -> setText("Очікує");
                        case "FINISHED" -> setText("Завершений");
                        default -> setText(status);
                    }
                }
            }
        });
        
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("Переглянути");
            
            
            {
                viewButton.setOnAction(event -> handleViewAuction(getTableRow().getItem()));
                
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Auction auction = getTableRow().getItem();
                    if (auction != null && auction.getStatus().equals("ACTIVE")) {
                        HBox buttons = new HBox(5, viewButton);
                        setGraphic(buttons);
                    } else {
                        setGraphic(viewButton);
                    }
                }
            }
        });
    }

    private void loadAuctions() {
        try {
            // Зберігаємо поточний вибір
            Auction selectedAuction = auctionsTable.getSelectionModel().getSelectedItem();
            String currentFilter = filterBox.getValue();
            
            // Оновлюємо дані
            auctions = FXCollections.observableArrayList(auctionService.findAll());
            filteredAuctions = new FilteredList<>(auctions);
            auctionsTable.setItems(filteredAuctions);
            
            filterBox.setValue(currentFilter);
            applyFilters();
            
            // Відновлюємо вибір
            if (selectedAuction != null) {
                auctionsTable.getSelectionModel().select(
                    auctions.stream()
                        .filter(a -> a.getId().equals(selectedAuction.getId()))
                        .findFirst()
                        .orElse(null)
                );
            }
            
            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Помилка при завантаженні аукціонів: " + e.getMessage());
        }
    }

    private void applyFilters() {
        if (filteredAuctions == null) return;
        
        String searchText = searchField.getText().toLowerCase();
        String filter = filterBox.getValue();
        String brandFilter = brandFilterBox != null ? brandFilterBox.getValue() : "Всі бренди";
        
        // Отримуємо значення фільтрів за ціною
        Double minPrice = null;
        Double maxPrice = null;
        if (minPriceField != null && !minPriceField.getText().isEmpty()) {
            try {
                minPrice = Double.parseDouble(minPriceField.getText());
            } catch (NumberFormatException e) {
                
            }
        }
        if (maxPriceField != null && !maxPriceField.getText().isEmpty()) {
            try {
                maxPrice = Double.parseDouble(maxPriceField.getText());
            } catch (NumberFormatException e) {
                
            }
        }
        
        // Отримуємо значення фільтрів за пробігом
        Integer minMileage = null;
        Integer maxMileage = null;
        if (minMileageField != null && !minMileageField.getText().isEmpty()) {
            try {
                minMileage = Integer.parseInt(minMileageField.getText());
            } catch (NumberFormatException e) {
                
            }
        }
        if (maxMileageField != null && !maxMileageField.getText().isEmpty()) {
            try {
                maxMileage = Integer.parseInt(maxMileageField.getText());
            } catch (NumberFormatException e) {
                
            }
        }
        
        
        final Double finalMinPrice = minPrice;
        final Double finalMaxPrice = maxPrice;
        final Integer finalMinMileage = minMileage;
        final Integer finalMaxMileage = maxMileage;
        
        filteredAuctions.setPredicate(auction -> {
            if (auction == null || auction.getVehicleInfo() == null) return false;
            
            // Перевірка на відповідність пошуковому запиту
            boolean matchesSearch = auction.getVehicleInfo()
                .toLowerCase()
                .contains(searchText);
            
            // Перевірка на відповідність фільтру за статусом
            boolean matchesStatusFilter = switch (filter) {
                case "Активні" -> "ACTIVE".equals(auction.getStatus());
                case "Завершені" -> "FINISHED".equals(auction.getStatus());
                case "Мої аукціони" -> {
                    Long currentUserId = Navigator.getCurrentUser().getId();
                    Long auctionUserId = auction.getUserId();
                    yield currentUserId != null && currentUserId.equals(auctionUserId);
                }
                default -> true;
            };
            
            // Перевірка на відповідність фільтру за брендом
            boolean matchesBrandFilter = brandFilter.equals("Всі бренди") || 
                                        auction.getVehicleInfo().contains(brandFilter);
            
            // Перевірка на відповідність фільтру за ціною
            boolean matchesPriceFilter = true;
            if (finalMinPrice != null && auction.getCurrentPrice() < finalMinPrice) {
                matchesPriceFilter = false;
            }
            if (finalMaxPrice != null && auction.getCurrentPrice() > finalMaxPrice) {
                matchesPriceFilter = false;
            }
            
            // Перевірка на відповідність фільтру за пробігом
            boolean matchesMileageFilter = true;
            // Отримуємо транспортний засіб для перевірки пробігу
            if (finalMinMileage != null || finalMaxMileage != null) {
                try {
                    Vehicle vehicle = vehicleService.findById(auction.getVehicleId());
                    if (vehicle != null) {
                        Integer mileage = vehicle.getMileage();
                        if (mileage != null) {
                            if (finalMinMileage != null && mileage < finalMinMileage) {
                                matchesMileageFilter = false;
                            }
                            if (finalMaxMileage != null && mileage > finalMaxMileage) {
                                matchesMileageFilter = false;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ігноруємо помилки при отриманні транспортного засобу
                    System.err.println("Помилка при отриманні транспортного засобу: " + e.getMessage());
                }
            }
            
            return matchesSearch && matchesStatusFilter && matchesBrandFilter && 
                   matchesPriceFilter && matchesMileageFilter;
        });
        
        updateStatistics();
    }

    private void updateStatistics() {
        totalAuctionsLabel.setText("Всього аукціонів: " + auctions.size());
        long activeCount = auctions.stream()
            .filter(a -> a.getStatus().equals("ACTIVE"))
            .count();
        activeAuctionsLabel.setText("Активних: " + activeCount);
    }

    @FXML
    private void handleCreateAuction() {
        Stage stage = (Stage) searchField.getScene().getWindow();
        Navigator.navigateToCreateAuction(stage);
    }

    @FXML
    private void handleProfile() {
        Stage stage = (Stage) auctionsTable.getScene().getWindow();
        Navigator.navigateToProfile(stage);
    }
    
    @FXML
    private void handleAdminDashboard() {
        // Перевіряємо чи користувач є адміністратором
        if (!userService.isAdmin(Navigator.getCurrentUser().getId())) {
            showAlert("Доступ заборонено", "Тільки адміністратори мають доступ до цієї сторінки.");
            return;
        }
        
        Stage stage = (Stage) auctionsTable.getScene().getWindow();
        Navigator.navigateToAdminDashboard(stage);
    }

    @FXML
    private void handleLogout() {
        Navigator.setCurrentUser(null);
        Stage stage = (Stage) searchField.getScene().getWindow();
        Navigator.navigateToLogin(stage);
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleMyVehicles() {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        Navigator.navigateToMyVehicles(stage);
    }

    private void handleViewAuction(Auction auction) {
        Stage stage = (Stage) searchField.getScene().getWindow();
        Navigator.navigateToAuction(stage, auction.getId());
    }

    private void handlePlaceBid(Auction auction) {
        
    }

    @FXML
    private void handleRefresh() {
        loadAuctions(); 
    }

    private void startUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
        updateTimer = new Timer(true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    try {
                        loadAuctions(); 
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Помилка при оновленні: " + e.getMessage());
                    }
                });
            }
        }, 60000, 60000); // Оновлюємо кожну хвилину
    }

    public void cleanup() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }
} 