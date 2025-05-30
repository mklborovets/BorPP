package com.auction.controller;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

import com.auction.model.TransmissionType;
import com.auction.model.Vehicle;
import com.auction.model.VehicleCondition;
import com.auction.model.VehicleType;
import com.auction.service.VehicleService;
import com.auction.util.Navigator;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class EditVehicleController implements Initializable {
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField yearField;
    @FXML private ComboBox<VehicleType> typeComboBox;
    @FXML private ComboBox<VehicleCondition> conditionComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private TextField vinField;
    @FXML private TextField mileageField;
    @FXML private TextField engineField;
    @FXML private TextField engineVolumeField;
    @FXML private TextField powerField;
    @FXML private ComboBox<TransmissionType> transmissionComboBox;
    @FXML private TextArea documentsArea;
    @FXML private TextField photoUrlField;
    @FXML private ImageView photoPreview;
    @FXML private Label noPhotoLabel;
    
    private final VehicleService vehicleService = new VehicleService();
    private Vehicle vehicle;
    private File selectedPhotoFile;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
    }
    
    private void setupComboBoxes() {
        typeComboBox.setItems(FXCollections.observableArrayList(VehicleType.values()));
        conditionComboBox.setItems(FXCollections.observableArrayList(VehicleCondition.values()));
        transmissionComboBox.setItems(FXCollections.observableArrayList(TransmissionType.values()));
    }
    
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        loadVehicleData();
    }
    
    private void loadVehicleData() {
        brandField.setText(vehicle.getBrand());
        modelField.setText(vehicle.getModel());
        yearField.setText(String.valueOf(vehicle.getYear()));

        for (VehicleType type : VehicleType.values()) {
            if (type.getDisplayName().equals(vehicle.getType())) {
                typeComboBox.setValue(type);
                break;
            }
        }

        for (VehicleCondition condition : VehicleCondition.values()) {
            if (condition.getDisplayName().equals(vehicle.getCondition())) {
                conditionComboBox.setValue(condition);
                break;
            }
        }
        
        descriptionArea.setText(vehicle.getDescription());
        vinField.setText(vehicle.getVin());
        mileageField.setText(vehicle.getMileage() != null ? String.valueOf(vehicle.getMileage()) : "");
        engineField.setText(vehicle.getEngine());
        engineVolumeField.setText(String.valueOf(vehicle.getEngineVolume()));
        powerField.setText(vehicle.getPower() != null ? String.valueOf(vehicle.getPower()) : "");
        
        
        for (TransmissionType transmission : TransmissionType.values()) {
            if (transmission.getDisplayName().equals(vehicle.getTransmission())) {
                transmissionComboBox.setValue(transmission);
                break;
            }
        }
        
        documentsArea.setText(vehicle.getDocuments());
        
        // Завантаження фотографії, якщо вона є
        if (vehicle.getPhotoUrl() != null && !vehicle.getPhotoUrl().isEmpty()) {
            photoUrlField.setText(vehicle.getPhotoUrl());
            try {
                // Завантажуємо фото з ресурсів
                String photoPath = "file:src/main/resources/" + vehicle.getPhotoUrl();
                Image image = new Image(photoPath);
                photoPreview.setImage(image);
                noPhotoLabel.setVisible(false);
            } catch (Exception e) {
                System.err.println("Помилка при завантаженні зображення: " + e.getMessage());
                noPhotoLabel.setVisible(true);
            }
        } else {
            photoUrlField.setText("");
            photoPreview.setImage(null);
            noPhotoLabel.setVisible(true);
        }
    }
    
    @FXML
    private void handleSave() {
        try {
            vehicle.setBrand(brandField.getText());
            vehicle.setModel(modelField.getText());
            vehicle.setYear(Integer.parseInt(yearField.getText()));
            vehicle.setType(typeComboBox.getValue().getDisplayName());
            vehicle.setCondition(conditionComboBox.getValue().getDisplayName());
            vehicle.setDescription(descriptionArea.getText());
            vehicle.setVin(vinField.getText());
            vehicle.setMileage(Integer.parseInt(mileageField.getText()));
            vehicle.setEngine(engineField.getText());
            vehicle.setEngineVolume(Double.parseDouble(engineVolumeField.getText()));
            vehicle.setPower(powerField.getText().isEmpty() ? null : Integer.parseInt(powerField.getText()));
            vehicle.setTransmission(transmissionComboBox.getValue().getDisplayName());
            vehicle.setDocuments(documentsArea.getText());
            
            // Обробка фотографії
            String photoUrl = photoUrlField.getText().trim();
            if (selectedPhotoFile != null) {
                // Якщо вибрано новий файл, копіюємо його в папку з фотографіями
                photoUrl = savePhotoFile(selectedPhotoFile, vehicle.getBrand() + "_" + vehicle.getModel());
                vehicle.setPhotoUrl(photoUrl);
            }
            
            vehicleService.update(vehicle);
            
            Stage stage = (Stage) brandField.getScene().getWindow();
            Navigator.navigateToMyVehicles(stage);
        } catch (NumberFormatException e) {
            showError("Будь ласка, перевірте правильність введених числових значень");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) brandField.getScene().getWindow();
        Navigator.navigateToMyVehicles(stage);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void handleChoosePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Вибрати фотографію");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Зображення", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(photoUrlField.getScene().getWindow());
        if (selectedFile != null) {
            selectedPhotoFile = selectedFile;
            photoUrlField.setText(selectedFile.getAbsolutePath());
            
            // Відображення вибраної фотографії
            try {
                Image image = new Image(selectedFile.toURI().toString());
                photoPreview.setImage(image);
                noPhotoLabel.setVisible(false);
            } catch (Exception e) {
                showError("Помилка при завантаженні зображення: " + e.getMessage());
            }
        }
    }
    
    private String savePhotoFile(File sourceFile, String baseFileName) throws Exception {
        // Створюємо директорію для фотографій, якщо вона не існує
        Path photosDir = Paths.get("src", "main", "resources", "photos");
        if (!Files.exists(photosDir)) {
            Files.createDirectories(photosDir);
        }
        
        // Генеруємо унікальне ім'я файлу
        String extension = sourceFile.getName().substring(sourceFile.getName().lastIndexOf("."));
        String fileName = baseFileName.replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + extension;
        Path targetPath = photosDir.resolve(fileName);
        
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Повертаємо відносний шлях до файлу для збереження в базі даних
        return "photos/" + fileName;
    }
}