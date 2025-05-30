package com.auction.controller;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

import com.auction.exception.ServiceException;
import com.auction.model.TransmissionType;
import com.auction.model.Vehicle;
import com.auction.model.VehicleCondition;
import com.auction.model.VehicleType;
import com.auction.service.VehicleService;
import com.auction.util.Navigator;
import com.auction.util.ValidationUtils;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AddVehicleController implements Initializable {
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField yearField;
    @FXML private ComboBox<VehicleType> typeComboBox;
    @FXML private ComboBox<VehicleCondition> conditionComboBox;
    @FXML private TextField vinField;
    @FXML private TextField mileageField;
    @FXML private TextField engineField;
    @FXML private TextField engineVolumeField;
    @FXML private TextField powerField;
    @FXML private ComboBox<TransmissionType> transmissionComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea documentsArea;
    @FXML private TextField photoUrlField;
    @FXML private ImageView photoPreview;
    @FXML private Label noPhotoLabel;
    @FXML private Label messageLabel;
    
    private final VehicleService vehicleService = new VehicleService();
    private File selectedPhotoFile;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBoxes();
    }
    
    private void setupComboBoxes() {
        typeComboBox.getItems().setAll(VehicleType.values());
        conditionComboBox.getItems().setAll(VehicleCondition.values());
        transmissionComboBox.getItems().setAll(TransmissionType.values());
    }
    
    @FXML
    private void handleSave() {
        try {
            validateFields();
            
            Vehicle vehicle = new Vehicle();
            vehicle.setBrand(brandField.getText().trim());
            vehicle.setModel(modelField.getText().trim());
            vehicle.setYear(Integer.parseInt(yearField.getText().trim()));
            vehicle.setType(typeComboBox.getValue().getDisplayName());
            vehicle.setCondition(conditionComboBox.getValue().getDisplayName());
            vehicle.setVin(vinField.getText().trim());
            vehicle.setMileage(Integer.parseInt(mileageField.getText().trim()));
            vehicle.setEngine(engineField.getText().trim());
            vehicle.setEngineVolume(Double.parseDouble(engineVolumeField.getText().trim()));
            vehicle.setPower(powerField.getText().trim().isEmpty() ? null : Integer.parseInt(powerField.getText().trim()));
            vehicle.setTransmission(transmissionComboBox.getValue().getDisplayName());
            vehicle.setDescription(descriptionArea.getText().trim());
            vehicle.setDocuments(documentsArea.getText().trim());
            vehicle.setUserId(Navigator.getCurrentUser().getId());
            
            // Обробка фотографії
            String photoUrl = photoUrlField.getText().trim();
            if (selectedPhotoFile != null) {
                photoUrl = savePhotoFile(selectedPhotoFile, vehicle.getBrand() + "_" + vehicle.getModel());
            }
            
            vehicleService.addVehicle(
                vehicle.getUserId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getType(),
                vehicle.getCondition(),
                vehicle.getVin(),
                vehicle.getMileage(),
                vehicle.getEngine(),
                vehicle.getEngineVolume(),
                vehicle.getPower(),
                vehicle.getTransmission(),
                vehicle.getEngine(),                 
                vehicle.getDocuments(),
                vehicle.getDescription(),
                photoUrl, 
                null 
            );
            
            showSuccess("Транспортний засіб успішно додано");
            handleBack();
            
        } catch (NumberFormatException e) {
            showError("Перевірте правильність числових значень");
        } catch (ServiceException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Помилка при збереженні: " + e.getMessage());
        }
    }
    
    private void validateFields() {
        if (brandField.getText().trim().isEmpty() ||
            modelField.getText().trim().isEmpty() ||
            yearField.getText().trim().isEmpty() ||
            typeComboBox.getValue() == null ||
            conditionComboBox.getValue() == null ||
            vinField.getText().trim().isEmpty() ||
            mileageField.getText().trim().isEmpty() ||
            engineField.getText().trim().isEmpty() ||
            engineVolumeField.getText().trim().isEmpty()) {
            throw new ServiceException("Будь ласка, заповніть всі обов'язкові поля");
        }
        
        int year = Integer.parseInt(yearField.getText().trim());
        if (year < 1900 || year > 2024) {
            throw new ServiceException("Некоректний рік випуску");
        }
        
        if (!ValidationUtils.isValidVin(vinField.getText().trim())) {
            throw new ServiceException("Некоректний VIN-код");
        }
        
        int mileage = Integer.parseInt(mileageField.getText().trim());
        if (mileage < 0) {
            throw new ServiceException("Пробіг не може бути від'ємним");
        }
        
        double engineVolume = Double.parseDouble(engineVolumeField.getText().trim());
        if (engineVolume <= 0) {
            throw new ServiceException("Об'єм двигуна повинен бути більше 0");
        }
    }
    
    @FXML
    private void handleBack() {
        Stage stage = (Stage) brandField.getScene().getWindow();
        Navigator.navigateToMyVehicles(stage);
    }
    
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: red;");
    }
    
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: green;");
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
        Path photosDir = Paths.get("src", "main", "resources", "photos");
        if (!Files.exists(photosDir)) {
            Files.createDirectories(photosDir);
        }
        
        String extension = sourceFile.getName().substring(sourceFile.getName().lastIndexOf("."));
        String fileName = baseFileName.replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + extension;
        Path targetPath = photosDir.resolve(fileName);
        
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        return "photos/" + fileName;
    }
}