package com.auction.controller;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import com.auction.model.Vehicle;
import com.auction.service.VehicleService;
import com.auction.util.Navigator;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MyVehiclesController implements Initializable {
    @FXML private TableView<Vehicle> vehiclesTable;
    @FXML private TableColumn<Vehicle, String> brandColumn;
    @FXML private TableColumn<Vehicle, String> modelColumn;
    @FXML private TableColumn<Vehicle, Integer> yearColumn;
    @FXML private TableColumn<Vehicle, String> typeColumn;
    @FXML private TableColumn<Vehicle, String> engineColumn;
    @FXML private TableColumn<Vehicle, Integer> mileageColumn;
    @FXML private TableColumn<Vehicle, String> registrationDateColumn;
    @FXML private TableColumn<Vehicle, Void> actionsColumn;
    @FXML private Label totalVehiclesLabel;
    @FXML private Label userLabel;
    
    private final VehicleService vehicleService = new VehicleService();
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        brandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        engineColumn.setCellValueFactory(new PropertyValueFactory<>("engine"));
        
        mileageColumn.setCellValueFactory(new PropertyValueFactory<>("mileage"));
        mileageColumn.setCellFactory(col -> new TableCell<Vehicle, Integer>() {
            @Override
            protected void updateItem(Integer mileage, boolean empty) {
                super.updateItem(mileage, empty);
                if (empty || mileage == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d км", mileage));
                }
            }
        });
        
        registrationDateColumn.setCellValueFactory(data ->
            javafx.beans.binding.Bindings.createStringBinding(
                () -> data.getValue().getRegistrationDate().format(DATE_FORMATTER)
            )
        );
        
        
        actionsColumn.setCellFactory(col -> new TableCell<Vehicle, Void>() {
            private final Button viewButton = new Button("Переглянути");
            private final Button editButton = new Button("Редагувати");
            private final Button deleteButton = new Button("Видалити");
            
            {
                viewButton.setOnAction(event -> {
                    Vehicle vehicle = getTableView().getItems().get(getIndex());
                    handleViewVehicle(vehicle);
                });
                
                editButton.setOnAction(event -> {
                    Vehicle vehicle = getTableView().getItems().get(getIndex());
                    handleEditVehicle(vehicle);
                });
                
                deleteButton.setOnAction(event -> {
                    Vehicle vehicle = getTableView().getItems().get(getIndex());
                    handleDeleteVehicle(vehicle);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, viewButton, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
        
        
        loadVehicles();
        
        
        userLabel.setText(Navigator.getCurrentUser().getUsername());
    }
    
    private void loadVehicles() {
        List<Vehicle> vehicles = vehicleService.findByUserId(Navigator.getCurrentUser().getId());
        vehiclesTable.setItems(FXCollections.observableArrayList(vehicles));
        totalVehiclesLabel.setText(String.valueOf(vehicles.size()));
    }
    
    @FXML
    private void handleBack() {
        Stage stage = (Stage) vehiclesTable.getScene().getWindow();
        Navigator.navigateToMain(stage);
    }
    
    @FXML
    private void handleAddVehicle() {
        Stage stage = (Stage) vehiclesTable.getScene().getWindow();
        Navigator.navigateToAddVehicle(stage);
    }
    
    private void handleViewVehicle(Vehicle vehicle) {
        Stage stage = (Stage) vehiclesTable.getScene().getWindow();
        Navigator.navigateToVehicleDetails(stage, vehicle);
    }
    
    private void handleEditVehicle(Vehicle vehicle) {
        Stage stage = (Stage) vehiclesTable.getScene().getWindow();
        Navigator.navigateToEditVehicle(stage, vehicle);
    }
    
    private void handleDeleteVehicle(Vehicle vehicle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Видалення транспортного засобу");
        alert.setHeaderText(null);
        alert.setContentText("Ви впевнені, що хочете видалити цей транспортний засіб?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                vehicleService.delete(vehicle.getId());
                loadVehicles();
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Помилка");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Не вдалося видалити транспортний засіб: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }
} 