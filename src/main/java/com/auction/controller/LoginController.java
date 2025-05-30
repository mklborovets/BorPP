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

public class LoginController {
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label messageLabel;
    
    private final UserService userService = new UserService();
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Будь ласка, заповніть всі поля");
            return;
        }
        
        try {
            User user = userService.authenticate(username, password);
            Navigator.setCurrentUser(user);
            showSuccess("Успішний вхід!");
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Navigator.navigateToMain(stage);
        } catch (ServiceException e) {
            showError(e.getMessage());
        }
    }
    
    @FXML
    private void handleRegister() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        Navigator.navigateToRegister(stage);
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