package com.auction.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.collections.FXCollections;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;

import com.auction.model.Vehicle;
import com.auction.model.Auction;
import com.auction.service.AuctionService;
import com.auction.service.VehicleService;
import com.auction.util.Navigator;
import com.auction.exception.ServiceException;

public class CreateAuctionController implements Initializable {
    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private TextField startPriceField;
    @FXML private TextField priceStepField;
    @FXML private DatePicker startDatePicker;
    @FXML private ComboBox<String> startTimeComboBox;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> endTimeComboBox;
    @FXML private Label messageLabel;
    
    private final AuctionService auctionService = new AuctionService();
    private final VehicleService vehicleService = new VehicleService();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Завантажуємо транспортні засоби користувача
        List<Vehicle> userVehicles = vehicleService.findByUser(Navigator.getCurrentUser().getId());
        vehicleComboBox.setItems(FXCollections.observableArrayList(userVehicles));
        
        // Налаштовуємо відображення транспортних засобів у випадаючому списку
        vehicleComboBox.setConverter(new StringConverter<Vehicle>() {
            @Override
            public String toString(Vehicle vehicle) {
                if (vehicle == null) return null;
                return vehicle.getBrand() + " " + vehicle.getModel() + " (" + vehicle.getYear() + ")";
            }
            
            @Override
            public Vehicle fromString(String string) {
                return null; // Не потрібно, оскільки це тільки для відображення
            }
        });
        
        // Встановлюємо поточну дату як мінімальну
        LocalDate now = LocalDate.now();
        startDatePicker.setValue(now);
        endDatePicker.setValue(now);
        startDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisabled(empty || date.isBefore(now));
            }
        });
        endDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisabled(empty || date.isBefore(startDatePicker.getValue()));
            }
        });
        
        // Ініціалізуємо списки часу з інтервалом у 30 хвилин
        initializeTimeComboBoxes();
    }
    
    @FXML
    private void handleCreate() {
        try {
            // Отримуємо вибраний транспортний засіб
            Vehicle vehicle = vehicleComboBox.getValue();
            if (vehicle == null) {
                showError("Будь ласка, виберіть транспортний засіб");
                return;
            }
            
            // Парсимо ціни
            double startPrice;
            double priceStep;
            try {
                startPrice = Double.parseDouble(startPriceField.getText().trim());
                priceStep = Double.parseDouble(priceStepField.getText().trim());
            } catch (NumberFormatException e) {
                showError("Будь ласка, введіть коректні числові значення для цін");
                return;
            }
            
            // Парсимо дати та час
            LocalDateTime startDateTime = parseDateTime(startDatePicker.getValue(), startTimeComboBox.getValue());
            LocalDateTime endDateTime = parseDateTime(endDatePicker.getValue(), endTimeComboBox.getValue());
            
            if (startDateTime == null || endDateTime == null) {
                showError("Будь ласка, введіть коректні значення дати та часу");
                return;
            }
            
            // Створюємо аукціон
            Auction auction = new Auction();
            auction.setUserId(Navigator.getCurrentUser().getId());
            auction.setVehicleId(vehicle.getId());
            auction.setStartPrice(startPrice);
            auction.setPriceStep(priceStep);
            auction.setStartTime(startDateTime);
            auction.setEndTime(endDateTime);
            auction.setStatus("PENDING"); // ДОДАЙ ЦЕ!

            auctionService.save(auction);
            
            showSuccess("Аукціон успішно створено!");
            
            // Повертаємось на головний екран
            Stage stage = (Stage) startPriceField.getScene().getWindow();
            Navigator.navigateToMain(stage);
            
        } catch (ServiceException e) {
            showError(e.getMessage());
        }
    }
    
    @FXML
    private void handleBack() {
        Stage stage = (Stage) startPriceField.getScene().getWindow();
        Navigator.navigateToMain(stage);
    }
    
    private LocalDateTime parseDateTime(LocalDate date, String timeStr) {
        try {
            if (timeStr == null || timeStr.isEmpty()) {
                return null;
            }
            LocalTime time = LocalTime.parse(timeStr.trim(), DateTimeFormatter.ofPattern("HH:mm"));
            return LocalDateTime.of(date, time);
        } catch (Exception e) {
            return null;
        }
    }
    
    private void initializeTimeComboBoxes() {
        // Створюємо список часу з інтервалом у 30 хвилин
        List<String> timeOptions = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            timeOptions.add(String.format("%02d:00", hour));
            timeOptions.add(String.format("%02d:30", hour));
        }
        
        // Заповнюємо ComboBox'и
        startTimeComboBox.setItems(FXCollections.observableArrayList(timeOptions));
        endTimeComboBox.setItems(FXCollections.observableArrayList(timeOptions));
        
        // Встановлюємо поточний час (округлений до 30 хвилин) за замовчуванням
        LocalTime now = LocalTime.now();
        int minutes = now.getMinute() >= 30 ? 30 : 0;
        LocalTime roundedTime = LocalTime.of(now.getHour(), minutes);
        String defaultTime = roundedTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        
        startTimeComboBox.setValue(defaultTime);
        endTimeComboBox.setValue(defaultTime);
    }
    
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: red;");
    }
    
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: green;");
    }
}