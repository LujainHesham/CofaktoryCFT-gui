package com.cofaktory.footprint.controllers;

import com.cofaktory.footprint.dao.jdbcImpl.UserDaoImpl;
import com.cofaktory.footprint.model.User;
import com.cofaktory.footprint.config.DatabaseConnection;
import com.cofaktory.footprint.myExceptions.DataAccessException;
import com.cofaktory.footprint.util.AppContext;
import com.cofaktory.footprint.util.PasswordHasher;
import com.cofaktory.footprint.util.WindowManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.geometry.NodeOrientation;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class AuthController implements Initializable {

    @FXML
    private Label emailLabel;
    @FXML
    private Label passwordLabel;
    @FXML
    private TextField emailField;
    @FXML
    private ToggleButton languageToggle;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private StackPane rootPane;
    @FXML
    private TextField passwordFieldVisible;
    @FXML
    private FontAwesomeIconView passwordVisibilityIcon;
    
    private boolean isEnglish = true;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up password field listeners
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            passwordFieldVisible.setText(newValue);
        });

        passwordFieldVisible.textProperty().addListener((observable, oldValue, newValue) -> {
            passwordField.setText(newValue);
        });

        // Defer window setup until after scene is loaded
        Platform.runLater(() -> {
            if (rootPane != null) {
                WindowManager.setupWindowDragging(rootPane);

                // Get the stage
                Stage stage = (Stage) rootPane.getScene().getWindow();

                // Only maximize if it's not already maximized
                if (!stage.isMaximized()) {
                    stage.setMaximized(true);
                }
            }
        });
    }


    @FXML
    private void handleLogin(ActionEvent event) {
        String userEmail = emailField.getText();
        String password = passwordField.isVisible() ? passwordField.getText() : passwordFieldVisible.getText();

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/carbon_footprint_tracker", "coffee_user", "1234")) {
            String sql = "SELECT UserID, UserRole, ForcePasswordChange FROM User WHERE UserEmail = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userEmail);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("UserID");
                String userRole = rs.getString("UserRole");
                boolean forceChange = rs.getBoolean("ForcePasswordChange");

                // Hash password and check (secure version)
                UserDaoImpl userDao = new UserDaoImpl(DatabaseConnection.getDataSource());
                User user = userDao.getById(userId);
                if (user != null && PasswordHasher.verifyPassword(password, user.getSalt(), user.getPassword())) {
                    if (forceChange) {
                        // Prompt for password change
                        promptPasswordReset(userDao, user, event);
                    } else {
                        openDashboard(userRole, userId);
                    }
                } else {
                    showAlert(isEnglish ? "Invalid credentials" : "بيانات الاعتماد غير صالحة");
                }
            } else {
                showAlert(isEnglish ? "Invalid credentials" : "بيانات الاعتماد غير صالحة");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(isEnglish ? "Database error" : "خطأ في قاعدة البيانات");
        }
    }

    private void promptPasswordReset(UserDaoImpl userDao, User user, ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(isEnglish ? "Password Reset Required" : "مطلوب إعادة تعيين كلمة المرور");
        dialog.setHeaderText(isEnglish ? "Please set a new password." : "يرجى تعيين كلمة مرور جديدة.");
        dialog.setContentText(isEnglish ? "New Password:" : "كلمة المرور الجديدة:");

        dialog.showAndWait().ifPresent(newPassword -> {
            try {
                String newSalt = PasswordHasher.generateSalt();
                String newHash = PasswordHasher.hashPassword(newPassword, newSalt);
                user.setPassword(newHash);
                user.setSalt(newSalt);
                user.setForcePasswordChange(false);
                userDao.update(user);

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(isEnglish ? "Success" : "نجاح");
                alert.setHeaderText(null);
                alert.setContentText(isEnglish ? "Password updated successfully." : "تم تحديث كلمة المرور بنجاح.");
                alert.showAndWait();
                
                // Proceed to dashboard after password update
                openDashboard(user.getUserRole(), user.getUserId());
            } catch (Exception ex) {
                showAlert(isEnglish ? "Password update failed." : "فشل تحديث كلمة المرور.");
            }
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(isEnglish ? "Error" : "خطأ");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Loads the dashboard for the given role and injects userId and branchId where needed.
     */
    private void openDashboard(String role, int userId) {
        try {
            String fxmlFile;
            boolean isBranchUser = false;
            switch (role) {
                case "BranchUser":
                    fxmlFile = "branch_user_dashboard.fxml";
                    isBranchUser = true;
                    break;
                case "OPManager":
                    fxmlFile = "op_manager_dashboard.fxml";
                    break;
                case "CIO":
                    fxmlFile = "cio_dashboard.fxml";
                    break;
                case "CEO":
                    fxmlFile = "ceoDashboard.fxml";
                    break;
                default:
                    fxmlFile = "login.fxml";
            }

            System.out.println("Loading dashboard: " + fxmlFile + " for role: " + role + " and userId: " + userId);

            // Use a more reliable way to load FXML files
            String fullPath = "/views/" + fxmlFile;
            System.out.println("Attempting to load: " + fullPath);

            // Try different approaches to load the resource
            java.net.URL resourceUrl = getClass().getResource(fullPath);
            if (resourceUrl == null) {
                resourceUrl = getClass().getClassLoader().getResource(fullPath.substring(1));
                System.out.println("Trying classloader with: " + fullPath.substring(1));
            }
            if (resourceUrl == null) {
                resourceUrl = getClass().getClassLoader().getResource("views/" + fxmlFile);
                System.out.println("Trying with views/ prefix: " + "views/" + fxmlFile);
            }

            System.out.println("Final resource URL: " + resourceUrl);
            if (resourceUrl == null) {
                throw new IOException("Could not find resource: " + fullPath);
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent dashboard = loader.load();

            // Inject userId and branchId into the controller if this is the BranchUser view
            if (isBranchUser) {
                UserDaoImpl userDao = new UserDaoImpl(DatabaseConnection.getDataSource());
                User u = userDao.getById(userId);
                int branchId = u.getBranchId();

                Object controller = loader.getController();
                if (controller instanceof BranchDataEntryController branchController) {
                    branchController.setUserAndBranch(userId, branchId);
                }
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(dashboard);
            scene.getStylesheets().add(getClass().getResource("/views/styles.css").toExternalForm());

            // Display with maximizing after scene change
            stage.setScene(scene);
            stage.setTitle(role.substring(0, 1).toUpperCase() + role.substring(1) + " Dashboard");

            // Show first, then maximize to ensure proper rendering
            stage.show();

            // Maximize the window
            Platform.runLater(() -> {
                stage.setMaximized(true);
            });
        } catch (IOException e) {
            System.err.println("IOException loading dashboard: " + e.getMessage());
            e.printStackTrace();
            showAlert(isEnglish ? "Dashboard loading error (IO): " + e.getMessage() : "خطأ في تحميل لوحة المعلومات");
        } catch (DataAccessException e) {
            System.err.println("DataAccessException loading dashboard: " + e.getMessage());
            e.printStackTrace();
            showAlert(isEnglish ? "Dashboard loading error (Data): " + e.getMessage() : "خطأ في تحميل لوحة المعلومات");
        } catch (Exception e) {
            System.err.println("Unexpected exception loading dashboard: " + e.getMessage());
            e.printStackTrace();
            showAlert(isEnglish ? "Dashboard loading error (Unexpected): " + e.getMessage() : "خطأ في تحميل لوحة المعلومات");
        }
    }

    @FXML
    public void toggleLanguage(ActionEvent event) {
        isEnglish = !isEnglish;
        setLanguage(isEnglish);
    }

    private void setLanguage(boolean english) {
        if (english) {
            languageToggle.setText("عربي");
            emailLabel.setText("Email");
            passwordLabel.setText("Password");
            loginButton.setText("Login");
            languageToggle.getScene().getRoot().setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        } else {
            languageToggle.setText("English");
            emailLabel.setText("البريد الإلكتروني");
            passwordLabel.setText("كلمة المرور");
            loginButton.setText("تسجيل الدخول");
            languageToggle.getScene().getRoot().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }
    }

    @FXML
    public void togglePasswordVisibility() {
        if (passwordField.isVisible()) {
            // Switch to visible password (eye icon)
            passwordFieldVisible.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordFieldVisible.setVisible(true);
            passwordFieldVisible.setManaged(true);
            passwordVisibilityIcon.setGlyphName("EYE"); // Show that clicking will hide password
        } else {
            // Switch to hidden password (eye slash icon)
            passwordField.setText(passwordFieldVisible.getText());
            passwordFieldVisible.setVisible(false);
            passwordFieldVisible.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisibilityIcon.setGlyphName("EYE_SLASH"); // Show that clicking will show password
        }
    }
}