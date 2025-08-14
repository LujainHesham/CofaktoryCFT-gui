package com.cofaktory.footprint.util;

import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class WindowManager {

    private static double xOffset = 0;
    private static double yOffset = 0;
    // Maximum safe dimensions to avoid texture overflow errors
    private static final double MAX_SAFE_WIDTH = 1920;
    private static final double MAX_SAFE_HEIGHT = 1080;

    /**
     * Sets up window dragging for a custom window
     * @param root The parent node to attach dragging to
     */
    public static void setupWindowDragging(Parent root) {
        // Add null check to prevent NullPointerException
        if (root == null) {
            System.err.println("Warning: Attempted to setup window dragging on null root. Skipping setup.");
            return;
        }

        root.setOnMousePressed(event -> {
            // Only enable dragging from top 50px of the window
            if (event.getY() <= 50) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        root.setOnMouseDragged(event -> {
            if (root.getScene() != null && root.getScene().getWindow() != null && yOffset <= 50) {
                Stage stage = (Stage) root.getScene().getWindow();
                // Only drag if not maximized, otherwise restore first
                if (stage.isMaximized()) {
                    // Calculate position for restored window
                    double percentageX = event.getScreenX() / stage.getWidth();
                    stage.setMaximized(false);

                    // Position window so mouse remains at relatively the same spot on the title bar
                    stage.setX(event.getScreenX() - (xOffset * percentageX));
                    stage.setY(0); // Put at top of screen
                } else {
                    // Regular window dragging
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            }
        });
    }

    /**
     * Alternative method to set up window dragging after scene is available
     */
    public static void setupWindowDraggingLater(Scene scene) {
        Parent root = scene.getRoot();
        setupWindowDragging(root);
    }

    /**
     * Handle minimizing the window from an event
     * @param event ActionEvent from a button
     */
    public static void handleMinimize(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.setIconified(true);
    }

    /**
     * Handle maximizing/restoring the window from an event
     * @param event ActionEvent from a button
     * @param maximizeButton The button used for maximizing (optional for text updating)
     */
    public static void handleMaximize(ActionEvent event, Button maximizeButton) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();

        if (stage.isMaximized()) {
            stage.setMaximized(false);
        } else {
            // Use proper maximization instead of custom sizing
            stage.setMaximized(true);
        }

        // Update button text if provided
        if (maximizeButton != null) {
            // Use FontAwesomeIconView to update the icon
            Node graphic = maximizeButton.getGraphic();
            if (graphic instanceof de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView iconView) {
                iconView.setGlyphName(stage.isMaximized() ? "COMPRESS" : "EXPAND");
            }
        }
    }

    /**
     * Handle closing the window from an event
     * @param event ActionEvent from a button
     */
    public static void handleClose(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    /**
     * Sets a window to true fullscreen mode
     * @param stage The stage to resize
     */
    public static void setFullScreen(Stage stage) {
        if (stage == null) return;

        // First ensure we're not maximized to avoid state conflicts
        if (stage.isMaximized()) {
            stage.setMaximized(false);
        }

        // Short delay to ensure state changes are applied
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Now set to maximized
        stage.setMaximized(true);
    }

    /**
     * Opens a window in true fullscreen mode
     * @param scene The scene to show
     * @param stage The stage to set fullscreen
     */
    public static void openInFullScreen(Scene scene, Stage stage) {
        stage.setScene(scene);
        setFullScreen(stage);
        stage.show();
    }

    // Original methods for direct Stage manipulation
    public static void minimizeStage(Stage stage) {
        stage.setIconified(true);
    }

    public static void closeStage(Stage stage) {
        stage.close();
    }
}