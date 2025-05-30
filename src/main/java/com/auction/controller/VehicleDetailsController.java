package com.auction.controller;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import com.auction.exception.ServiceException;
import com.auction.model.User;
import com.auction.model.Vehicle;
import com.auction.service.AuctionService;
import com.auction.service.UserService;
import com.auction.service.VehicleService;
import com.auction.util.Navigator;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class VehicleDetailsController implements Initializable {
    @FXML private Label brandLabel;
    @FXML private Label modelLabel;
    @FXML private Label yearLabel;
    @FXML private Label typeLabel;
    @FXML private Label conditionLabel;
    @FXML private Label engineLabel;
    @FXML private Label engineVolumeLabel;
    @FXML private Label powerLabel;
    @FXML private Label mileageLabel;
    @FXML private Label transmissionLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Label ownerLabel;
    @FXML private Label registrationDateLabel;
    @FXML private Label documentsLabel;
    @FXML private Button createAuctionButton;
    
    private final VehicleService vehicleService = new VehicleService();
    private final UserService userService = new UserService();
    private final AuctionService auctionService = new AuctionService();
    
    private Vehicle vehicle;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Ініціалізація відбудеться після встановлення транспортного засобу
    }
    
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        loadVehicleData();
    }
    
    private void loadVehicleData() {
        try {
            
            User owner = userService.findById(vehicle.getUserId());
            
            // Оновлюємо основну інформацію
            brandLabel.setText(vehicle.getBrand());
            modelLabel.setText(vehicle.getModel());
            yearLabel.setText(String.valueOf(vehicle.getYear()));
            typeLabel.setText(vehicle.getType());
            conditionLabel.setText(vehicle.getCondition());
            
            // Оновлюємо технічні характеристики
            engineLabel.setText(vehicle.getEngine());
            engineVolumeLabel.setText(String.format("%.1f л", vehicle.getEngineVolume()));
            powerLabel.setText(String.format("%d к.с.", vehicle.getPower()));
            mileageLabel.setText(String.format("%d км", vehicle.getMileage()));
            transmissionLabel.setText(vehicle.getTransmission());
            
            // Оновлюємо опис
            descriptionArea.setText(vehicle.getDescription());
            
            // Оновлюємо додаткову інформацію
            ownerLabel.setText(owner.getUsername());
            registrationDateLabel.setText(vehicle.getRegistrationDate().format(DATE_FORMATTER));
            documentsLabel.setText(vehicle.getDocuments());
            
            // Перевіряємо чи може поточний користувач створювати аукціон
            boolean canCreateAuction = vehicle.getUserId().equals(Navigator.getCurrentUser().getId()) ||
                                    userService.isAdmin(Navigator.getCurrentUser().getId());
            createAuctionButton.setVisible(canCreateAuction);
            
        } catch (ServiceException e) {
            showError(e.getMessage());
        }
    }
    
    @FXML
    private void handleBack() {
        Stage stage = (Stage) brandLabel.getScene().getWindow();
        Navigator.navigateToMain(stage);
    }
    
    @FXML
    private void handleCreateAuction() {
        Stage stage = (Stage) brandLabel.getScene().getWindow();
        Navigator.navigateToCreateAuction(stage);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 