package com.auction.controller;

import com.auction.exception.ServiceException;
import com.auction.model.User;
import com.auction.service.UserService;
import com.auction.util.Navigator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {
    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Label messageLabel;
    
    private final UserService userService = new UserService();
    
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Валідація полів
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Будь ласка, заповніть всі поля");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Паролі не співпадають");
            return;
        }
        
        try {
            User user = userService.register(username, password, email);
            Navigator.setCurrentUser(user);
            showSuccess("Реєстрація успішна!");
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Navigator.navigateToMain(stage);
        } catch (ServiceException e) {
            showError(e.getMessage());
        }
    }
    
    @FXML
    private void handleBack() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        Navigator.navigateToLogin(stage);
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