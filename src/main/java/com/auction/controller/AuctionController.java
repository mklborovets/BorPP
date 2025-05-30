package com.auction.controller;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import com.auction.exception.ServiceException;
import com.auction.model.Auction;
import com.auction.model.Bid;
import com.auction.model.Vehicle;
import com.auction.service.AuctionService;
import com.auction.service.BidService;
import com.auction.service.UserService;
import com.auction.service.VehicleService;
import com.auction.util.Navigator;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class AuctionController implements Initializable {
    @FXML private Label vehicleLabel;
    @FXML private Label startPriceLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label priceStepLabel;
    @FXML private Label startTimeLabel;
    @FXML private Label endTimeLabel;
    @FXML private Label statusLabel;
    @FXML private ImageView vehiclePhotoView;
    @FXML private Label noPhotoLabel;
    @FXML private Label userBalanceLabel;
    @FXML private TextField bidAmountField;
    @FXML private Button placeBidButton;
    @FXML private Button placeStepBidButton;
    @FXML private TableView<Bid> bidsTable;
    @FXML private TableColumn<Bid, String> bidderColumn;
    @FXML private TableColumn<Bid, Double> amountColumn;
    @FXML private TableColumn<Bid, String> timeColumn;
    
    private final AuctionService auctionService = new AuctionService();
    private final BidService bidService = new BidService();
    private final VehicleService vehicleService = new VehicleService();
    private final UserService userService = new UserService();
    
    private Auction auction;
    private Timer updateTimer;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBidsTable();
    }
    
    public void setAuction(Auction auction) {
        this.auction = auction;
        loadAuctionData();
        checkAuctionStatus(); 
        startUpdateTimer();
    }
    
    private void setupBidsTable() {
        bidderColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        timeColumn.setCellValueFactory(data -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> data.getValue().getCreatedAt().format(DATE_TIME_FORMATTER)
            )
        );
    }
    
    private void loadAuctionData() {
        try {
            Vehicle vehicle = vehicleService.findById(auction.getVehicleId());
            vehicleLabel.setText(vehicle.getBrand() + " " + vehicle.getModel());
            startPriceLabel.setText(String.format("%.2f грн", auction.getStartPrice()));
            priceStepLabel.setText(String.format("%.2f грн", auction.getPriceStep()));
            currentPriceLabel.setText(String.format("%.2f грн", auctionService.getCurrentPrice(auction.getId())));
            
            // Відображення балансу користувача
            double userBalance = userService.getBalance(Navigator.getCurrentUser().getId());
            userBalanceLabel.setText(String.format("%.2f грн", userBalance));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            startTimeLabel.setText(auction.getStartTime().format(formatter));
            endTimeLabel.setText(auction.getEndTime().format(formatter));
            
            statusLabel.setText(auction.getStatus());
            
            // Завантаження та відображення фотографії транспортного засобу
            loadVehiclePhoto(vehicle);
            if ("ACTIVE".equals(auction.getStatus())) {
                statusLabel.setStyle("-fx-text-fill: green;");
            } else if ("PENDING".equals(auction.getStatus())) {
                statusLabel.setStyle("-fx-text-fill: blue;");
            } else if ("FINISHED".equals(auction.getStatus())) {
                statusLabel.setStyle("-fx-text-fill: black;");
            } else {
                statusLabel.setStyle("-fx-text-fill: red;");
            }
            
            loadBids();
            
            boolean isOwner = vehicle.getUserId().equals(Navigator.getCurrentUser().getId());
            boolean isActive = "ACTIVE".equals(auction.getStatus());
            placeBidButton.setDisable(!isActive || isOwner);
            placeStepBidButton.setDisable(!isActive || isOwner);
            
        } catch (ServiceException e) {
            showError(e.getMessage());
        }
    }
    
    private void loadBids() {
        bidsTable.setItems(FXCollections.observableArrayList(
            bidService.findByAuction(auction.getId())
        ));
    }
    
    private void updateCurrentPrice() {
        double currentPrice = auctionService.getCurrentPrice(auction.getId());
        currentPriceLabel.setText(String.format("%.2f грн", currentPrice));
    }
    
    @FXML
    private void handlePlaceBid() {
        try {
            double amount = Double.parseDouble(bidAmountField.getText());
            auctionService.placeBid(Navigator.getCurrentUser().getId(), auction.getId(), amount);
            bidAmountField.clear();
            loadBids();
            updateCurrentPrice();
            
            // Оновлюємо дані користувача та баланс
            Navigator.refreshCurrentUser();
            double userBalance = userService.getBalance(Navigator.getCurrentUser().getId());
            userBalanceLabel.setText(String.format("%.2f грн", userBalance));
        } catch (NumberFormatException e) {
            showError("Будь ласка, введіть коректну суму");
        } catch (ServiceException e) {
            showError(e.getMessage());
        }
    }
    
    @FXML
    private void handlePlaceStepBid() {
        try {
            double currentPrice = auctionService.getCurrentPrice(auction.getId());
            double priceStep = auction.getPriceStep();

            double newBidAmount = currentPrice + priceStep;

            auctionService.placeBid(Navigator.getCurrentUser().getId(), auction.getId(), newBidAmount);

            loadBids();
            updateCurrentPrice();
            
            // Оновлюємо дані користувача та баланс
            Navigator.refreshCurrentUser();
            double userBalance = userService.getBalance(Navigator.getCurrentUser().getId());
            userBalanceLabel.setText(String.format("%.2f грн", userBalance));
        } catch (ServiceException e) {
            showError(e.getMessage());
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadBids();
        updateCurrentPrice();
        
        // Оновлюємо баланс користувача
        Navigator.refreshCurrentUser();
        double userBalance = userService.getBalance(Navigator.getCurrentUser().getId());
        userBalanceLabel.setText(String.format("%.2f грн", userBalance));
    }
    
    private void startUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
        updateTimer = new Timer(true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    // Перезавантажуємо аукціон, щоб оновити його статус
                    try {
                        Navigator.refreshCurrentUser();

                        auction = auctionService.findById(auction.getId());
                        loadAuctionData();
                        checkAuctionStatus();
                    } catch (Exception e) {
                        showError("Помилка оновлення даних аукціону: " + e.getMessage());
                    }
                });
            }
        }, 60000, 60000); // Оновлюємо кожну хвилину, синхронно з MainController
    }
    
    private void showError(String message) {
        // Показуємо помилку на UI
        statusLabel.setText("ПОМИЛКА: " + message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }
    
    /**
     * Перевіряє статус аукціону і оновлює UI відповідно
     * Якщо аукціон завершено, показує інформацію про переможця
     */
    private void checkAuctionStatus() {
        if ("FINISHED".equals(auction.getStatus())) {
            // Аукціон завершено, показуємо інформацію про переможця
            try {
                List<Bid> bids = bidService.findByAuction(auction.getId());
                if (bids.isEmpty()) {
                    statusLabel.setText("Аукціон завершено без ставок");
                    statusLabel.setStyle("-fx-text-fill: orange;");
                } else {
                    // Знаходимо найвищу ставку (перша в списку, бо вони відсортовані)
                    Bid winningBid = bids.get(0);
                    String winnerUsername = winningBid.getUsername();
                    double winningAmount = winningBid.getAmount();
                    
                    // Перевіряємо чи поточний користувач є переможцем
                    boolean isCurrentUserWinner = winningBid.getUserId().equals(Navigator.getCurrentUser().getId());
                    
                    if (isCurrentUserWinner) {
                        statusLabel.setText("Вітаємо! Ви виграли аукціон з сумою " + 
                                           String.format("%.2f грн", winningAmount));
                        statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        statusLabel.setText("Аукціон завершено. Переможець: " + winnerUsername + 
                                           " (" + String.format("%.2f грн", winningAmount) + ")");
                        statusLabel.setStyle("-fx-text-fill: blue;");
                    }
                }
            } catch (Exception e) {
                showError("Помилка при отриманні інформації про переможця: " + e.getMessage());
            }
        } else if ("CANCELLED".equals(auction.getStatus())) {
            statusLabel.setText("Аукціон скасовано");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    public void cleanup() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }
    
    /**
     * Завантажує та відображає фотографію транспортного засобу
     * @param vehicle Транспортний засіб
     */
    private void loadVehiclePhoto(Vehicle vehicle) {
        try {
            String photoUrl = vehicle.getPhotoUrl();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                // Створюємо шлях до фотографії
                String resourcePath = "/" + photoUrl;
                Image image = new Image(getClass().getResourceAsStream(resourcePath));
                vehiclePhotoView.setImage(image);
                noPhotoLabel.setVisible(false);
            } else {
                // Якщо фотографія відсутня
                vehiclePhotoView.setImage(null);
                noPhotoLabel.setVisible(true);
            }
        } catch (Exception e) {
            // У разі помилки завантаження фотографії
            System.err.println("Помилка завантаження фотографії: " + e.getMessage());
            vehiclePhotoView.setImage(null);
            noPhotoLabel.setVisible(true);
        }
    }
    
    @FXML
    private void handleBack() {
        cleanup();
        Stage stage = (Stage) vehicleLabel.getScene().getWindow();
        Navigator.navigateToMain(stage);
    }
} 