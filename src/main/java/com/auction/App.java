package com.auction;

import com.auction.util.IconManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        primaryStage.setTitle("Аукціон транспортних засобів");
        primaryStage.setScene(new Scene(root));
        
        // Встановлюємо іконку для вікна
        IconManager.setAppIcon(primaryStage);
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 