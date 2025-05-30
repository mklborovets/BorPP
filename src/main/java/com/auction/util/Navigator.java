package com.auction.util;

import java.io.IOException;

import com.auction.controller.AuctionController;
import com.auction.controller.EditVehicleController;
import com.auction.controller.VehicleDetailsController;
import com.auction.model.User;
import com.auction.model.Vehicle;
import com.auction.service.AuctionService;
import com.auction.service.UserService;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigator {
    private static User currentUser;
    
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static void refreshCurrentUser() {
        if (currentUser != null) {
            try {
                UserService userService = new UserService();
                currentUser = userService.findById(currentUser.getId());
            } catch (Exception e) {
                System.err.println("Error refreshing current user: " + e.getMessage());
            }
        }
    }
    
    public static void navigateTo(Stage stage, String fxml) {
        try {
            Parent root = FXMLLoader.load(Navigator.class.getResource("/fxml/" + fxml));
            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                stage.getScene().setRoot(root);
            }
        } catch (IOException | IllegalStateException e) {
            throw new RuntimeException("Помилка навігації: " + e.getMessage(), e);
        }
    }
    
    public static void navigateToMain(Stage stage) {
        try {
            Parent root = FXMLLoader.load(
                Navigator.class.getResource("/fxml/main.fxml")
            );
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void navigateToLogin(Stage stage) {
        navigateTo(stage, "login.fxml");
    }
    
    public static void navigateToRegister(Stage stage) {
        navigateTo(stage, "register.fxml");
    }
    
    public static void navigateToProfile(Stage stage) {
        navigateTo(stage, "profile.fxml");
    }
    
    public static void navigateToAuction(Stage stage, Long auctionId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                Navigator.class.getResource("/fxml/auction.fxml")
            );
            Parent root = loader.load();
            
            // Передаємо аукціон в контролер
            AuctionController controller = loader.getController();
            controller.setAuction(new AuctionService().findById(auctionId));
            
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void navigateToCreateAuction(Stage stage) {
        try {
            Parent root = FXMLLoader.load(
                Navigator.class.getResource("/fxml/create-auction.fxml")
            );
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void navigateToVehicleDetails(Stage stage, Vehicle vehicle) {
        try {
            FXMLLoader loader = new FXMLLoader(
                Navigator.class.getResource("/fxml/vehicle-details.fxml")
            );
            Parent root = loader.load();
            
            // Передаємо транспортний засіб в контролер
            VehicleDetailsController controller = loader.getController();
            controller.setVehicle(vehicle);
            
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void navigateToMyVehicles(Stage stage) {
        try {
            Parent root = FXMLLoader.load(
                Navigator.class.getResource("/fxml/my-vehicles.fxml")
            );
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void navigateToAddVehicle(Stage stage) {
        try {
            Parent root = FXMLLoader.load(
                Navigator.class.getResource("/fxml/add-vehicle.fxml")
            );
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void navigateToEditVehicle(Stage stage, Vehicle vehicle) {
        try {
            FXMLLoader loader = new FXMLLoader(
                Navigator.class.getResource("/fxml/edit-vehicle.fxml")
            );
            Parent root = loader.load();
            
            EditVehicleController controller = loader.getController();
            controller.setVehicle(vehicle);
            
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void navigateToAdminDashboard(Stage stage) {
        try {
            Parent root = FXMLLoader.load(
                Navigator.class.getResource("/fxml/admin-dashboard.fxml")
            );
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 