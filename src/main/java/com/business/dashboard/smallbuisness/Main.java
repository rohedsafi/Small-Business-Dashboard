package com.business.dashboard.smallbuisness;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Initializing database...");
            DatabaseConnection.initializeDatabase();

            // Load the FXML from resources — path matches package `smallbuisness`
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/business/dashboard/smallbuisness/dashboard.fxml"));
            Scene scene = new Scene(loader.load());
            
            primaryStage.setTitle("Business Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}