package com.cofaktory.footprint;

import com.cofaktory.footprint.util.AppContext;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the root FXML
        Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/views/styles.css").toExternalForm());

        // Configure stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("Carbon Footprint Tracker - Login");

        // Set reasonable initial size (will be maximized later)
        primaryStage.setWidth(1024);
        primaryStage.setHeight(768);

        // Show the stage first
        primaryStage.show();

        // Maximize after showing to prevent flickering
        Platform.runLater(() -> {
            primaryStage.setMaximized(true);
        });

        // Store screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        AppContext.getInstance().setScreenWidth(screenBounds.getWidth());
        AppContext.getInstance().setScreenHeight(screenBounds.getHeight());
    }

    public static void main(String[] args) {
        launch(args);
    }
}