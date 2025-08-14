package com.cofaktory.footprint.controllers;

import com.cofaktory.footprint.config.DatabaseConnection;
import com.cofaktory.footprint.dao.jdbcImpl.*;
import com.cofaktory.footprint.model.*;
import com.cofaktory.footprint.util.SessionManager;
import com.cofaktory.footprint.myExceptions.DataAccessException;
import com.cofaktory.footprint.util.PasswordHasher;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.cofaktory.footprint.service.ReportGenerationService;
import com.cofaktory.footprint.service.LanguageService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.sql.Date;
import java.util.List;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;


public class OPManagerDashboardController {

    @FXML private Label currentUserLabel;
    @FXML private Label currentBranchLabel;
    @FXML public ComboBox<Branch> branchComboBox;


    // --- USERS TAB ---
    @FXML public TextField userNameField;
    @FXML public ComboBox<String> userRoleComboBox;
    @FXML public TextField userEmailField;
    @FXML public PasswordField userPasswordField;
    @FXML public TableView<User> userTable;
    @FXML public TableColumn<User, Integer> userIDColumn;
    @FXML public TableColumn<User, String> userNameColumn;
    @FXML public TableColumn<User, String> userRoleColumn;
    @FXML public TableColumn<User, String> userEmailColumn;
    @FXML public TableColumn<User, String> userPasswordColumn;
    @FXML public TableColumn<User, Integer> userBranchIDColumn;
    @FXML public TableColumn<User, Boolean> forcePasswordChangeColumn;

    // --- BRANCHES TAB ---
    @FXML public TextField branchCityIdField;
    @FXML public TextField branchLocationField;
    @FXML public TextField branchNumEmployeesField;
    @FXML public TableView<Branch> branchTable;
    @FXML public TableColumn<Branch, Integer> branchIDColumn4;
    @FXML public TableColumn<Branch, Integer> cityIDColumn;
    @FXML public TableColumn<Branch, String> locationColumn;
    @FXML public TableColumn<Branch, Integer> numEmployeesColumn;
    @FXML private ComboBox<City> cityComboBox;
    private ObservableList<City> cities = FXCollections.observableArrayList();

    // --- PRODUCTION TAB ---
    @FXML public TextField supplierField;
    @FXML public ComboBox<String> coffeeTypeComboBox;
    @FXML public ComboBox<String> productTypeComboBox;
    @FXML public TextField quantityField;
    @FXML public DatePicker productionDatePicker;
    @FXML public TableView<CoffeeProduction> productionTable;
    @FXML public TableColumn<CoffeeProduction, Integer> productionIDColumn;
    @FXML public TableColumn<CoffeeProduction, Integer> productionBranchIDColumn;
    @FXML public TableColumn<CoffeeProduction, Integer> productionUserIDColumn;
    @FXML public TableColumn<CoffeeProduction, String> supplierColumn;
    @FXML public TableColumn<CoffeeProduction, String> coffeeTypeColumn;
    @FXML public TableColumn<CoffeeProduction, String> productTypeColumn;
    @FXML public TableColumn<CoffeeProduction, Double> quantityColumn;
    @FXML public TableColumn<CoffeeProduction, Date> productionDateColumn;
    @FXML public TableColumn<CoffeeProduction, Double> prCarbonEmissionsColumn;

    // --- PACKAGING TAB ---
    @FXML public TextField packagingWasteField;
    @FXML public DatePicker packagingDatePicker;
    @FXML public TableView<CoffeePackaging> packagingTable;
    @FXML public TableColumn<CoffeePackaging, Integer> packagingIDColumn;
    @FXML public TableColumn<CoffeePackaging, Integer> packagingBranchIDColumn;
    @FXML public TableColumn<CoffeePackaging, Integer> packagingUserIDColumn;
    @FXML public TableColumn<CoffeePackaging, Double> packagingWasteColumn;
    @FXML public TableColumn<CoffeePackaging, Double> paCarbonEmissionsColumn;
    @FXML public TableColumn<CoffeePackaging, Date> packagingDateColumn;

    // --- DISTRIBUTION TAB ---
    @FXML public ComboBox<String> vehicleTypeComboBox;
    @FXML public TextField numberOfVehiclesField;
    @FXML public TextField distancePerVehicleField;
    @FXML public DatePicker distributionDatePicker;
    @FXML public TableView<CoffeeDistribution> distributionTable;
    @FXML public TableColumn<CoffeeDistribution, Integer> distributionIDColumn;
    @FXML public TableColumn<CoffeeDistribution, Integer> distributionBranchIDColumn;
    @FXML public TableColumn<CoffeeDistribution, Integer> distributionUserIDColumn;
    @FXML public TableColumn<CoffeeDistribution, String> vehicleTypeColumn;
    @FXML public TableColumn<CoffeeDistribution, Integer> numberOfVehiclesColumn;
    @FXML public TableColumn<CoffeeDistribution, Double> distancePerVehicleColumn;
    @FXML public TableColumn<CoffeeDistribution, Double> totalDistanceColumn;
    @FXML public TableColumn<CoffeeDistribution, Double> fuelEfficiencyColumn;
    @FXML public TableColumn<CoffeeDistribution, Double> vCarbonEmissionsColumn;
    @FXML public TableColumn<CoffeeDistribution, Date> distributionDateColumn;

    private UserDaoImpl userDao;
    private BranchDaoImpl branchDao;
    private CoffeeProductionDaoImpl productionDao;
    private CoffeePackagingDaoImpl packagingDao;
    private CoffeeDistributionDaoImpl distributionDao;
    private CityDaoImpl cityDao;

    // --- REPORT TAB ---
    @FXML public TableView<Branch> reportBranchTable;
    @FXML public TableColumn<Branch, Integer> reportBranchIdColumn;
    @FXML public TableColumn<Branch, String> reportBranchLocationColumn;
    @FXML public Label reportStatusLabel;
    private ReportGenerationService reportGenerationService;

    public void initialize() {
        setupSessionInfo();
        userDao = new UserDaoImpl(DatabaseConnection.getDataSource());
        branchDao = new BranchDaoImpl(DatabaseConnection.getDataSource());
        productionDao = new CoffeeProductionDaoImpl(DatabaseConnection.getDataSource());
        packagingDao = new CoffeePackagingDaoImpl(DatabaseConnection.getDataSource());
        distributionDao = new CoffeeDistributionDaoImpl(DatabaseConnection.getDataSource());
        cityDao = new CityDaoImpl(DatabaseConnection.getDataSource());

        // Populate branch ComboBox
        branchComboBox.setCellFactory(param -> new ListCell<Branch>() {
            @Override
            protected void updateItem(Branch item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });

        branchComboBox.setButtonCell(new ListCell<Branch>() {
            @Override
            protected void updateItem(Branch item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });
        try {
            List<City> cities = cityDao.getAll();
            cityComboBox.setItems(FXCollections.observableArrayList(cities));

            // Set custom display
            cityComboBox.setCellFactory(param -> new ListCell<City>() {
                @Override
                protected void updateItem(City item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.toString());
                }
            });

            cityComboBox.setButtonCell(new ListCell<City>() {
                @Override
                protected void updateItem(City item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "Select City" : item.toString());
                }
            });

        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Failed to load cities: " + e.getMessage());
        }

        try {
            List<Branch> branches = branchDao.getAll();
            branchComboBox.setItems(FXCollections.observableArrayList(branches));
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Failed to load branches: " + e.getMessage());
        }

        // Show branch ComboBox only for BranchUser role
        userRoleComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isBranchUser = "BranchUser".equals(newVal);
            branchComboBox.setVisible(isBranchUser);
        });


        if (userRoleComboBox != null) {
            userRoleComboBox.setItems(FXCollections.observableArrayList("BranchUser", "OPManager", "CIO", "CEO"));
        }

        if (coffeeTypeComboBox != null) {
            coffeeTypeComboBox.setItems(FXCollections.observableArrayList("Arabica", "Robusta", "Blend"));
        }

        if (productTypeComboBox != null) {
            productTypeComboBox.setItems(FXCollections.observableArrayList("Beans", "Ground", "Capsules"));
        }

        if (vehicleTypeComboBox != null) {
            vehicleTypeComboBox.setItems(FXCollections.observableArrayList("Minivan", "Pickup Truck"));
        }


        setupUserColumns();
        setupBranchColumns();
        setupProductionColumns();
        setupPackagingColumns();
        setupDistributionColumns();

        handleRefreshUser();
        handleRefreshBranch();
        handleRefreshProduction();
        handleRefreshPackaging();
        handleRefreshDistribution();

        setupReportTab();
        refreshReportBranches();

        setSelectionListeners();
        reportGenerationService = new ReportGenerationService(new LanguageService());
    }

    private void setupSessionInfo() {
        if (SessionManager.isAuthenticated()) {
            currentUserLabel.setText("User ID: " + SessionManager.getCurrentUserId());
            currentBranchLabel.setText("Branch: " + SessionManager.getCurrentBranchName() +
                    " (ID: " + SessionManager.getCurrentBranchId() + ")");
        }
    }

    private void setupUserColumns() {
        userIDColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("userRole"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
        userPasswordColumn.setCellValueFactory(cellData -> new SimpleStringProperty("********")); // Always show stars
        userBranchIDColumn.setCellValueFactory(cellData -> {
            Integer branchId = cellData.getValue().getBranchId();
            return new SimpleObjectProperty<>(branchId);
        });
        
        // Configure forcePasswordChange column to display text instead of checkbox
        forcePasswordChangeColumn.setCellValueFactory(new PropertyValueFactory<>("forcePasswordChange"));
        forcePasswordChangeColumn.setCellFactory(column -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "True" : "False");
                }
            }
        });
    }

    private void setupBranchColumns() {
        branchIDColumn4.setCellValueFactory(new PropertyValueFactory<>("branchId"));
        cityIDColumn.setCellValueFactory(new PropertyValueFactory<>("cityId"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        numEmployeesColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfEmployees"));
    }

    private void setupProductionColumns() {
        productionIDColumn.setCellValueFactory(new PropertyValueFactory<>("productionId"));
        productionBranchIDColumn.setCellValueFactory(new PropertyValueFactory<>("branchId"));
        productionUserIDColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        coffeeTypeColumn.setCellValueFactory(new PropertyValueFactory<>("coffeeType"));
        productTypeColumn.setCellValueFactory(new PropertyValueFactory<>("productType"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("productionQuantitiesOfCoffeeKG"));
        prCarbonEmissionsColumn.setCellValueFactory(new PropertyValueFactory<>("prCarbonEmissionsKG"));
        productionDateColumn.setCellValueFactory(new PropertyValueFactory<>("productionDate"));

    }

    private void setupPackagingColumns() {
        System.out.println("Setting up packaging columns...");
        packagingIDColumn.setCellValueFactory(new PropertyValueFactory<>("packagingId"));
        packagingBranchIDColumn.setCellValueFactory(new PropertyValueFactory<>("branchId"));
        packagingUserIDColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        packagingWasteColumn.setCellValueFactory(new PropertyValueFactory<>("packagingWasteKG"));
        paCarbonEmissionsColumn.setCellValueFactory(new PropertyValueFactory<>("paCarbonEmissionsKG"));
        packagingDateColumn.setCellValueFactory(new PropertyValueFactory<>("packagingDate"));
        
        // Add debug information about the columns
        System.out.println("Packaging columns setup complete:");
        System.out.println("- ID column: " + packagingIDColumn.getCellValueFactory());
        System.out.println("- Branch ID column: " + packagingBranchIDColumn.getCellValueFactory());
        System.out.println("- User ID column: " + packagingUserIDColumn.getCellValueFactory());
        System.out.println("- Waste column: " + packagingWasteColumn.getCellValueFactory());
        System.out.println("- Carbon emissions column: " + paCarbonEmissionsColumn.getCellValueFactory());
        System.out.println("- Date column: " + packagingDateColumn.getCellValueFactory());
    }

    private void setupDistributionColumns() {
        distributionIDColumn.setCellValueFactory(new PropertyValueFactory<>("distributionId"));
        distributionBranchIDColumn.setCellValueFactory(new PropertyValueFactory<>("branchId"));
        distributionUserIDColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        vehicleTypeColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleType"));
        numberOfVehiclesColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfVehicles"));
        distancePerVehicleColumn.setCellValueFactory(new PropertyValueFactory<>("distancePerVehicleKM"));
        totalDistanceColumn.setCellValueFactory(new PropertyValueFactory<>("totalDistanceKM"));
        fuelEfficiencyColumn.setCellValueFactory(new PropertyValueFactory<>("fuelEfficiency"));
        distributionDateColumn.setCellValueFactory(new PropertyValueFactory<>("distributionDate"));
        vCarbonEmissionsColumn.setCellValueFactory(new PropertyValueFactory<>("vCarbonEmissionsKg"));

    }

    private void setupReportTab() {
        reportBranchIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getBranchId()).asObject());
        reportBranchLocationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation()));
    }

    private void setSelectionListeners() {
        // User Table
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, selected) -> {
            if (selected != null) {
                userNameField.setText(selected.getUserName());
                userRoleComboBox.setValue(selected.getUserRole());
                userEmailField.setText(selected.getUserEmail());
                userPasswordField.setText(selected.getPassword());

                // Handle branch selection for users
                if (selected.getBranchId() != null && selected.getBranchId() > 0) {
                    Branch userBranch = null;
                    try {
                        userBranch = branchDao.getById(selected.getBranchId());
                    } catch (Exception e) {
                        System.err.println("Error loading branch for user: " + e.getMessage());
                    }
                    
                    if (userBranch != null) {
                        branchComboBox.getSelectionModel().select(userBranch);
                        System.out.println("Selected branch ID " + userBranch.getBranchId() + " for user: " + selected.getUserName());
                    } else {
                        branchComboBox.getSelectionModel().clearSelection();
                    }
                } else {
                    branchComboBox.getSelectionModel().clearSelection();
                }
                
                // Make branch combo box visible only for BranchUser
                branchComboBox.setVisible("BranchUser".equals(selected.getUserRole()));
            }
        });

        // Branch Table
        branchTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, selected) -> {
            if (selected != null) {
                // Set fields directly from the selected branch
                branchLocationField.setText(selected.getLocation());
                branchNumEmployeesField.setText(String.valueOf(selected.getNumberOfEmployees()));

                // Find and select the corresponding city in the combo box
                try {
                    City city = cityDao.getById(selected.getCityId());
                    if (city != null) {
                        cityComboBox.getSelectionModel().select(city);
                        // This line is for debugging - prints the selected city
                        System.out.println("Selected city: " + city.getCityName() + " for branch: " + selected.getLocation());
                    } else {
                        System.out.println("City not found for ID: " + selected.getCityId());
                        cityComboBox.getSelectionModel().clearSelection();
                        // Fallback to just setting the city ID in the field
                        branchCityIdField.setText(String.valueOf(selected.getCityId()));
                    }
                } catch (Exception e) {
                    System.err.println("Error selecting city for branch: " + e.getMessage());
                    e.printStackTrace();
                    // Fallback to just setting the city ID in the field
                    branchCityIdField.setText(String.valueOf(selected.getCityId()));
                }
            }
        });

        // Production Table
        productionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, selected) -> {
            if (selected != null) {
                supplierField.setText(selected.getSupplier());
                coffeeTypeComboBox.setValue(selected.getCoffeeType());
                productTypeComboBox.setValue(selected.getProductType());
                quantityField.setText(String.valueOf(selected.getProductionQuantitiesOfCoffeeKG()));
                productionDatePicker.setValue(selected.getProductionDate() != null ?
                        selected.getProductionDate().toLocalDate() : null);
            }
        });

        // Packaging Table
        packagingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, selected) -> {
            if (selected != null) {
                // Only show editable fields
                packagingWasteField.setText(String.valueOf(selected.getPackagingWasteKG()));
                packagingDatePicker.setValue(selected.getPackagingDate() != null ?
                        selected.getPackagingDate().toLocalDate() : null);
            }
        });

        // Distribution Table
        distributionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, selected) -> {
            if (selected != null) {
                vehicleTypeComboBox.setValue(selected.getVehicleType());
                numberOfVehiclesField.setText(String.valueOf(selected.getNumberOfVehicles()));
                distancePerVehicleField.setText(String.valueOf(selected.getDistancePerVehicleKM()));
                distributionDatePicker.setValue(selected.getDistributionDate() != null ?
                        selected.getDistributionDate().toLocalDate() : null);
            }
        });
    }

    // ------ USERS TAB CRUD ------
    @FXML
    public void handleAddUser() {
        try {
            // Get the input values
            String name = userNameField.getText();
            String role = userRoleComboBox.getValue();
            String email = userEmailField.getText();
            String password = userPasswordField.getText();
            
            // Validate required fields
            if (name == null || name.trim().isEmpty()) {
                OPManagerTableUtils.showAlert("User name is required.");
                return;
            }
            if (role == null || role.trim().isEmpty()) {
                OPManagerTableUtils.showAlert("User role is required.");
                return;
            }
            if (email == null || email.trim().isEmpty()) {
                OPManagerTableUtils.showAlert("User email is required.");
                return;
            }
            if (password == null || password.trim().isEmpty()) {
                OPManagerTableUtils.showAlert("Password is required.");
                return;
            }
            
            // Generate salt for password hashing
            String salt = PasswordHasher.generateSalt();
            Integer branchId = null;

            // Only BranchUser role requires a branch assignment
            if ("BranchUser".equals(role)) {
                Branch selectedBranch = branchComboBox.getSelectionModel().getSelectedItem();
                if (selectedBranch == null) {
                    OPManagerTableUtils.showAlert("Please select a branch for BranchUser.");
                    return;
                }
                branchId = selectedBranch.getBranchId();
                System.out.println("Assigning user to branch ID: " + branchId);
            }

            // Hash the password
            String hashedPassword = PasswordHasher.hashPassword(password, salt);
            
            // Create the user object
            User newUser = new User(0, branchId, name, role, email, hashedPassword, salt, false);
            
            // Insert the user
            boolean success = userDao.insert(newUser);
            if (!success) {
                OPManagerTableUtils.showAlert("Failed to add user. Database error.");
                return;
            }
            
            System.out.println("Added new user with ID: " + newUser.getUserId());
            
            // Refresh the table and clear fields
            handleRefreshUser();
            OPManagerTableUtils.clearUserFields(this);
            OPManagerTableUtils.showInfo("User added successfully.");
        }
        catch (Exception e) {
            OPManagerTableUtils.showAlert("Add User failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleUpdateUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            // Update role and branch...
            if ("BranchUser".equals(selected.getUserRole())) {
                Branch selectedBranch = branchComboBox.getSelectionModel().getSelectedItem();
                if (selectedBranch == null) {
                    OPManagerTableUtils.showAlert("Branch selection is required for BranchUser.");
                    return;
                }
                selected.setBranchId(selectedBranch.getBranchId());
            } else {
                selected.setBranchId(null);
            }
            userDao.update(selected);
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Update User failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            OPManagerTableUtils.showAlert("Please select a record to delete.");
            return;
        }
        try {
            userDao.delete(selected);
            handleRefreshUser();
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Delete User failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleSaveUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            OPManagerTableUtils.showAlert("Please select a user to save.");
            return;
        }
        
        try {
            String name = userNameField.getText();
            String role = userRoleComboBox.getValue();
            String email = userEmailField.getText();
            
            // Validate required fields
            if (name == null || name.trim().isEmpty()) {
                OPManagerTableUtils.showAlert("User name is required.");
                return;
            }
            if (role == null || role.trim().isEmpty()) {
                OPManagerTableUtils.showAlert("User role is required.");
                return;
            }
            if (email == null || email.trim().isEmpty()) {
                OPManagerTableUtils.showAlert("User email is required.");
                return;
            }
            
            selected.setUserName(name);
            selected.setUserRole(role);
            selected.setUserEmail(email);
            
            // Handle branch selection for BranchUser
            if ("BranchUser".equals(role)) {
                Branch selectedBranch = branchComboBox.getSelectionModel().getSelectedItem();
                if (selectedBranch == null) {
                    OPManagerTableUtils.showAlert("Please select a branch for BranchUser.");
                    return;
                }
                selected.setBranchId(selectedBranch.getBranchId());
            } else {
                selected.setBranchId(null);
            }
            
            userDao.save(selected);
            handleRefreshUser();
            OPManagerTableUtils.showInfo("User saved successfully.");
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Save User failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefreshUser() {
        try {
            List<User> data = userDao.getAll();
            userTable.setItems(FXCollections.observableArrayList(data));
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Failed to refresh User table: " + e.getMessage());
        }
    }

    @FXML
    public void handleResetPassword() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            OPManagerTableUtils.showAlert("Select a user first.");
            return;
        }

        try {
            String tempPassword = generateRandomPassword();
            String salt = PasswordHasher.generateSalt();
            String hashedTempPassword = PasswordHasher.hashPassword(tempPassword, salt);

            selected.setPassword(hashedTempPassword);
            selected.setSalt(salt);
            selected.setForcePasswordChange(true);
            userDao.update(selected);

            OPManagerTableUtils.showInfo("Temporary password for user: " + selected.getUserEmail() + "\n\n" +
                    "Temp Password: " + tempPassword + "\n\n" +
                    "User must change password at next login.");
            handleRefreshUser();
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Reset failed: " + e.getMessage());
        }
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        return random.ints(12, 0, chars.length())
                .mapToObj(chars::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    // ------ BRANCHES TAB CRUD ------
    @FXML
    public void handleAddBranch() {
        try {
            City selectedCity = cityComboBox.getSelectionModel().getSelectedItem();
            if (selectedCity == null) {
                OPManagerTableUtils.showAlert("Please select a city!");
                return;
            }

            String location = branchLocationField.getText().trim();
            if (location.isEmpty()) {
                OPManagerTableUtils.showAlert("Location cannot be empty!");
                return;
            }
            
            String numEmployeesText = branchNumEmployeesField.getText().trim();
            if (numEmployeesText.isEmpty()) {
                OPManagerTableUtils.showAlert("Number of employees cannot be empty!");
                return;
            }
            
            int numEmployees = Integer.parseInt(numEmployeesText);
            if (numEmployees <= 0) {
                OPManagerTableUtils.showAlert("Number of employees must be greater than zero!");
                return;
            }

            // Check if we need to associate a user with this branch
            User selectedUser = null;
            // If implementing one-to-one relationship, check if we have a user to associate
            if (userTable.getSelectionModel().getSelectedItem() != null) {
                selectedUser = userTable.getSelectionModel().getSelectedItem();
                // Check if this user is already associated with a branch
                if (selectedUser.getBranchId() != null && selectedUser.getBranchId() > 0) {
                    OPManagerTableUtils.showAlert("Selected user is already associated with Branch ID: " + selectedUser.getBranchId() + 
                                                 "\nPlease select another user or create a new user for this branch.");
                    return;
                }
            }

            Branch newBranch = new Branch(
                    0, // Auto-generated ID
                    selectedCity.getCityId(),
                    location,
                    numEmployees
            );

            // Insert the new branch
            if (!branchDao.insert(newBranch)) {
                OPManagerTableUtils.showAlert("Failed to add branch!");
                return;
            }
            
            int branchId = newBranch.getBranchId();
            if (branchId <= 0) {
                OPManagerTableUtils.showAlert("Branch was created but couldn't retrieve its ID! Cannot create default records.");
                handleRefreshBranch();
                refreshBranchComboBox();
                OPManagerTableUtils.clearBranchFields(this);
                return;
            }
            
            System.out.println("Created new branch with ID: " + branchId);
            
            // If we have a user selected, associate it with this new branch
            if (selectedUser != null) {
                selectedUser.setBranchId(branchId);
                userDao.update(selectedUser);
                System.out.println("Associated User ID " + selectedUser.getUserId() + " with Branch ID " + branchId);
            }
            
            // Automatically create blank records for production, packaging, and distribution
            // If we have a selected user, use that user's ID, otherwise find or create a suitable user
            int userId = (selectedUser != null) ? selectedUser.getUserId() : findOrCreateUserForBranch(branchId);
            
            if (userId > 0) {
                createDefaultRecordsForBranch(branchId, userId);
            } else {
                OPManagerTableUtils.showAlert("Could not find or create a user for this branch. Default records will not be created.");
            }
            
            handleRefreshBranch(); // Refresh the branch table
            refreshBranchComboBox(); // Refresh branch selection in Users tab
            OPManagerTableUtils.clearBranchFields(this);
            OPManagerTableUtils.showInfo("Branch added successfully with default records.");

        } catch (NumberFormatException e) {
            OPManagerTableUtils.showAlert("Invalid number format for employees!");
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Error adding branch: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int findOrCreateUserForBranch(int branchId) {
        try {
            // First check if any BranchUser exists without a branch association
            List<User> availableUsers = userDao.getAll().stream()
                .filter(u -> "BranchUser".equals(u.getUserRole()) && (u.getBranchId() == null || u.getBranchId() == 0))
                .toList();
            
            if (!availableUsers.isEmpty()) {
                // Use the first available branch user
                User user = availableUsers.get(0);
                user.setBranchId(branchId);
                userDao.save(user);
                System.out.println("Associated existing User ID " + user.getUserId() + " with Branch ID " + branchId);
                return user.getUserId();
            } else {
                // Get the current logged-in user if it's an OPManager or higher role
                int currentUserId = SessionManager.getCurrentUserId();
                if (currentUserId > 0) {
                    return currentUserId;
                }
                
                // If no suitable user found and we're not logged in, find any user
                List<User> allUsers = userDao.getAll();
                if (!allUsers.isEmpty()) {
                    return allUsers.get(0).getUserId();
                }
            }
            
            // No users found at all
            return -1;
        } catch (Exception e) {
            System.err.println("Error finding user for branch: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    private void createDefaultRecordsForBranch(int branchId, int userId) {
        if (branchId <= 0) {
            System.err.println("Invalid branch ID: " + branchId);
            OPManagerTableUtils.showAlert("Cannot create default records: invalid branch ID " + branchId);
            return;
        }
        
        if (userId <= 0) {
            System.err.println("Invalid user ID: " + userId);
            OPManagerTableUtils.showAlert("Cannot create default records: invalid user ID " + userId);
            return;
        }
        
        System.out.println("Creating empty records for branch ID: " + branchId + " with user ID: " + userId);
        
        try {
            // First check if the branch exists
            if (!branchDao.branchExists(branchId)) {
                OPManagerTableUtils.showAlert("Branch ID " + branchId + " does not exist in the database.");
                return;
            }
            
            System.out.println("Using user ID: " + userId + " for branch ID: " + branchId);
            
            // Create empty production record
            boolean productionSuccess = createEmptyProductionRecord(branchId, userId);
            
            // Create empty packaging record
            boolean packagingSuccess = createEmptyPackagingRecord(branchId, userId);
            
            // Create empty distribution record
            boolean distributionSuccess = createEmptyDistributionRecord(branchId, userId);
            
            if (productionSuccess && packagingSuccess && distributionSuccess) {
                System.out.println("Successfully created all empty records for branch ID: " + branchId);
                handleRefreshProduction();
                handleRefreshPackaging();
                handleRefreshDistribution();
            } else {
                StringBuilder errorMsg = new StringBuilder("Some records could not be created: ");
                if (!productionSuccess) errorMsg.append("Production, ");
                if (!packagingSuccess) errorMsg.append("Packaging, ");
                if (!distributionSuccess) errorMsg.append("Distribution, ");
                OPManagerTableUtils.showAlert(errorMsg.substring(0, errorMsg.length() - 2));
            }
            
        } catch (Exception e) {
            System.err.println("Failed to create empty records for branch ID " + branchId + ": " + e.getMessage());
            e.printStackTrace();
            OPManagerTableUtils.showAlert("Error creating empty records: " + e.getMessage());
        }
    }

    private int getUserIdForBranch(int branchId) {
        try {
            // First, check if any users exist for this branch
            List<User> branchUsers = userDao.getAll().stream()
                .filter(u -> u.getBranchId() != null && u.getBranchId() == branchId)
                .toList();
            
            // Get a valid user ID for the branch (either a branch user or the current user or any user)
            if (!branchUsers.isEmpty()) {
                // Use the first user associated with this branch
                int userId = branchUsers.get(0).getUserId();
                System.out.println("Using existing branch user ID: " + userId);
                return userId;
            } else {
                // Use current session user or find any valid user
                int userId = SessionManager.getCurrentUserId();
                if (userId > 0) {
                    System.out.println("Using current session user ID: " + userId);
                    return userId;
                } else {
                    // Try to find any valid user
                    List<User> allUsers = userDao.getAll();
                    if (!allUsers.isEmpty()) {
                        userId = allUsers.get(0).getUserId();
                        System.out.println("Using first available user ID: " + userId);
                        return userId;
                    } else {
                        System.err.println("No users found in the system!");
                        return -1;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding user ID for branch: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    private boolean createEmptyProductionRecord(int branchId, int userId) {
        try {
            System.out.println("Creating empty production record...");
            CoffeeProduction production = new CoffeeProduction();
            production.setBranchId(branchId);
            production.setUserId(userId);
            
            // Set default values for required fields that match the DB schema constraints
            production.setSupplier("Default Supplier");
            production.setCoffeeType("Arabica Beans"); // Must match ENUM('Arabica Beans', 'Robusta Beans', 'Organic Beans')
            production.setProductType("Ground"); // Must match ENUM('Ground', 'Whole Bean', 'Instant')
            production.setProductionQuantitiesOfCoffeeKG(0.1); // Must be > 0 to satisfy CHECK constraint
            production.setProductionDate(new java.sql.Date(System.currentTimeMillis())); // Current date
            
            boolean success = productionDao.insert(production);
            if (success) {
                System.out.println("Created empty production record with ID: " + production.getProductionId());
            } else {
                System.err.println("Failed to create empty production record");
            }
            return success;
        } catch (Exception e) {
            System.err.println("Error creating production record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean createEmptyPackagingRecord(int branchId, int userId) {
        try {
            System.out.println("Creating empty packaging record...");
            CoffeePackaging packaging = new CoffeePackaging();
            packaging.setBranchId(branchId);
            packaging.setUserId(userId);
            
            // Set default values for required fields
            packaging.setPackagingWasteKG(0.0);
            packaging.setPackagingDate(new java.sql.Date(System.currentTimeMillis())); // Current date instead of null
            
            boolean success = packagingDao.insert(packaging);
            if (success) {
                System.out.println("Created empty packaging record with ID: " + packaging.getPackagingId());
            } else {
                System.err.println("Failed to create empty packaging record");
            }
            return success;
        } catch (Exception e) {
            System.err.println("Error creating packaging record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean createEmptyDistributionRecord(int branchId, int userId) {
        try {
            System.out.println("Creating empty distribution record...");
            CoffeeDistribution distribution = new CoffeeDistribution();
            distribution.setBranchId(branchId);
            distribution.setUserId(userId);
            
            // Set default values for required fields
            distribution.setVehicleType("Minivan"); // Non-null default value
            distribution.setNumberOfVehicles(1); // Must be at least 1 to satisfy check constraint
            distribution.setDistancePerVehicleKM(1.0); // Must be positive to satisfy check constraint
            distribution.setDistributionDate(new java.sql.Date(System.currentTimeMillis())); // Current date instead of null
            
            boolean success = distributionDao.insert(distribution);
            if (success) {
                System.out.println("Created empty distribution record with ID: " + distribution.getDistributionId());
            } else {
                System.err.println("Failed to create empty distribution record");
            }
            return success;
        } catch (Exception e) {
            System.err.println("Error creating distribution record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void refreshBranchComboBox() {
        try {
            List<Branch> branches = branchDao.getAll();
            branchComboBox.setItems(FXCollections.observableArrayList(branches));
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Failed to refresh branches: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateBranch() {
        Branch selected = branchTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            OPManagerTableUtils.showAlert("Please select a record to update.");
            return;
        }
        try {
            selected.setCityId(Integer.parseInt(branchCityIdField.getText()));
            selected.setLocation(branchLocationField.getText());
            selected.setNumberOfEmployees(Integer.parseInt(branchNumEmployeesField.getText()));
            branchDao.update(selected);
            handleRefreshBranch();
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Update Branch failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteBranch() {
        Branch selected = branchTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            OPManagerTableUtils.showAlert("Please select a record to delete.");
            return;
        }
        try {
            branchDao.delete(selected);
            handleRefreshBranch();
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Delete Branch failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleSaveBranch() {
        Branch selected = branchTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            OPManagerTableUtils.showAlert("Please select a branch to save.");
            return;
        }
        
        try {
            City selectedCity = cityComboBox.getSelectionModel().getSelectedItem();
            if (selectedCity == null) {
                selected.setCityId(Integer.parseInt(branchCityIdField.getText()));
            } else {
                selected.setCityId(selectedCity.getCityId());
            }
            
            selected.setLocation(branchLocationField.getText());
            selected.setNumberOfEmployees(Integer.parseInt(branchNumEmployeesField.getText()));
            
            branchDao.save(selected);
            handleRefreshBranch();
            OPManagerTableUtils.showInfo("Branch saved successfully.");
        } catch (NumberFormatException e) {
            OPManagerTableUtils.showAlert("Invalid number format for employees!");
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Save Branch failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefreshBranch() {
        try {
            List<Branch> data = branchDao.getAll();
            branchTable.setItems(FXCollections.observableArrayList(data));
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Failed to refresh Branch table: " + e.getMessage());
        }
    }

    // ------ PRODUCTION TAB CRUD ------
    @FXML
    public void handleAddProduction() {
        try {
            int branchId = SessionManager.getCurrentBranchId();
            int userId = SessionManager.getCurrentUserId();
            String supplier = supplierField.getText();
            String coffeeType = coffeeTypeComboBox.getValue();
            String productType = productTypeComboBox.getValue();
            double quantity = Double.parseDouble(quantityField.getText());
            Date date = Date.valueOf(productionDatePicker.getValue());

            CoffeeProduction prod = new CoffeeProduction(0, branchId, userId, supplier, coffeeType, productType, quantity, 0, date);
            productionDao.insert(prod);
            handleRefreshProduction();
            OPManagerTableUtils.clearProductionFields(this);
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Add Production failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateProduction() {
        CoffeeProduction selected = productionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            OPManagerTableUtils.showAlert("Please select a record to update.");
            return;
        }
        try {
            selected.setSupplier(supplierField.getText());
            selected.setCoffeeType(coffeeTypeComboBox.getValue());
            selected.setProductType(productTypeComboBox.getValue());
            selected.setProductionQuantitiesOfCoffeeKG(Double.parseDouble(quantityField.getText()));
            selected.setProductionDate(Date.valueOf(productionDatePicker.getValue()));
            productionDao.update(selected);
            handleRefreshProduction();
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Update Production failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteProduction() {
        CoffeeProduction selected = productionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            OPManagerTableUtils.showAlert("Please select a record to delete.");
            return;
        }
        try {
            productionDao.delete(selected);
            handleRefreshProduction();
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Delete Production failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleSaveProduction() {
        CoffeeProduction selected = productionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            OPManagerTableUtils.showAlert("Please select a production record to save.");
            return;
        }
        
        try {
            selected.setSupplier(supplierField.getText());
            selected.setCoffeeType(coffeeTypeComboBox.getValue());
            selected.setProductType(productTypeComboBox.getValue());
            selected.setProductionQuantitiesOfCoffeeKG(Double.parseDouble(quantityField.getText()));
            selected.setProductionDate(Date.valueOf(productionDatePicker.getValue()));
            
            productionDao.save(selected);
            handleRefreshProduction();
            OPManagerTableUtils.showInfo("Production record saved successfully.");
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Save Production failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefreshProduction() {
        try {
            List<CoffeeProduction> data = productionDao.getAll();
            productionTable.setItems(FXCollections.observableArrayList(data));
            productionTable.refresh(); // Force UI refresh
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Failed to refresh Production table: " + e.getMessage());
            e.printStackTrace(); // Log stack trace for debugging
        }
    }

    // ------ PACKAGING TAB CRUD ------
    @FXML
    public void handleAddPackaging() {
        try {
            // Get current session IDs
            int branchId = SessionManager.getCurrentBranchId();
            int userId = SessionManager.getCurrentUserId();

            // Get user input
            double waste = Double.parseDouble(packagingWasteField.getText());
            Date date = Date.valueOf(packagingDatePicker.getValue());

            CoffeePackaging pack = new CoffeePackaging(
                    0, // packagingId (auto-generated)
                    branchId,
                    userId,
                    waste,
                    0, // carbon emissions (calculated by DB/service)
                    date
            );

            packagingDao.insert(pack);
            handleRefreshPackaging();
            OPManagerTableUtils.clearPackagingFields(this);
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Add Packaging failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdatePackaging() {
        CoffeePackaging selected = packagingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            OPManagerTableUtils.showAlert("Please select a record to update.");
            return;
        }
        try {
            // Use session values instead of parsing labels
            selected.setBranchId(SessionManager.getCurrentBranchId());
            selected.setUserId(SessionManager.getCurrentUserId());

            selected.setPackagingWasteKG(Double.parseDouble(packagingWasteField.getText()));
            selected.setPackagingDate(Date.valueOf(packagingDatePicker.getValue()));
            packagingDao.update(selected);
            handleRefreshPackaging();
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Update Packaging failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeletePackaging() {
        CoffeePackaging selected = packagingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            OPManagerTableUtils.showAlert("Please select a record to delete.");
            return;
        }
        try {
            packagingDao.delete(selected);
            handleRefreshPackaging();
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Delete Packaging failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleSavePackaging() {
        CoffeePackaging selected = packagingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            OPManagerTableUtils.showAlert("Please select a packaging record to save.");
            return;
        }
        
        try {
            // Get the original record from the database if it exists
            CoffeePackaging originalRecord = null;
            if (selected.getPackagingId() > 0) {
                originalRecord = packagingDao.getById(selected.getPackagingId());
                
                if (originalRecord == null) {
                    OPManagerTableUtils.showAlert("Could not retrieve original record.");
                    return;
                }
                
                // Keep the original user ID to avoid foreign key issues
                selected.setUserId(originalRecord.getUserId());
                // Keep the branch ID as well
                selected.setBranchId(originalRecord.getBranchId());
            } else {
                // This is a new record, use current session values
                int currentBranchId = SessionManager.getCurrentBranchId();
                int currentUserId = SessionManager.getCurrentUserId();
                
                // Make sure we have valid IDs
                if (currentBranchId <= 0) {
                    OPManagerTableUtils.showAlert("Invalid branch ID. Please select a valid branch.");
                    return;
                }
                
                if (currentUserId <= 0) {
                    // Try to find a valid user ID for the current branch
                    try {
                        List<User> users = userDao.getAll();
                        User validUser = users.stream()
                            .filter(u -> u.getBranchId() != null && u.getBranchId() == currentBranchId)
                            .findFirst()
                            .orElse(null);
                        
                        if (validUser != null) {
                            currentUserId = validUser.getUserId();
                            System.out.println("Found valid user ID: " + currentUserId + " for branch: " + currentBranchId);
                        } else {
                            // Try to find any user with BranchUser role
                            validUser = users.stream()
                                .filter(u -> "BranchUser".equals(u.getUserRole()))
                                .findFirst()
                                .orElse(null);
                                
                            if (validUser != null) {
                                currentUserId = validUser.getUserId();
                                System.out.println("Using BranchUser with ID: " + currentUserId);
                            } else {
                                // Last resort: use the first user in the list
                                if (!users.isEmpty()) {
                                    currentUserId = users.get(0).getUserId();
                                    System.out.println("Using first available user with ID: " + currentUserId);
                                } else {
                                    OPManagerTableUtils.showAlert("No valid users found in the database.");
                                    return;
                                }
                            }
                        }
                    } catch (Exception e) {
                        OPManagerTableUtils.showAlert("Error finding valid user: " + e.getMessage());
                        return;
                    }
                }
                
                selected.setBranchId(currentBranchId);
                selected.setUserId(currentUserId);
            }
            
            // Validate input
            String wasteText = packagingWasteField.getText();
            if (wasteText == null || wasteText.trim().isEmpty()) {
                OPManagerTableUtils.showAlert("Packaging waste amount is required.");
                return;
            }
            
            try {
                double waste = Double.parseDouble(wasteText);
                if (waste < 0) {
                    OPManagerTableUtils.showAlert("Packaging waste cannot be negative.");
                    return;
                }
                selected.setPackagingWasteKG(waste);
            } catch (NumberFormatException e) {
                OPManagerTableUtils.showAlert("Invalid packaging waste amount. Please enter a valid number.");
                return;
            }
            
            // Handle date
            if (packagingDatePicker.getValue() == null) {
                OPManagerTableUtils.showAlert("Date is required.");
                return;
            }
            selected.setPackagingDate(java.sql.Date.valueOf(packagingDatePicker.getValue()));
            
            System.out.println("Saving packaging with ID: " + selected.getPackagingId());
            System.out.println("BranchID: " + selected.getBranchId());
            System.out.println("UserID: " + selected.getUserId());
            System.out.println("Waste: " + selected.getPackagingWasteKG());
            System.out.println("Date: " + selected.getPackagingDate());
            
            boolean success;
            if (selected.getPackagingId() > 0) {
                // Call update directly for existing records
                success = packagingDao.update(selected);
            } else {
                // Call insert directly for new records
                success = packagingDao.insert(selected);
            }
            
            if (success) {
                handleRefreshPackaging();
                OPManagerTableUtils.showInfo("Packaging record saved successfully.");
            } else {
                OPManagerTableUtils.showAlert("Failed to save packaging record.");
            }
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Save Packaging failed: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    @FXML
    public void handleRefreshPackaging() {
        try {
            System.out.println("Refreshing packaging data...");
            
            // Check if there are any records in the table
            checkPackagingTableExists();
            
            // Try direct SQL insert first
            insertTestPackagingDataWithSQL();
            
            // Now get the data using the DAO
            List<CoffeePackaging> existingData = packagingDao.getAll();
            System.out.println("Retrieved " + existingData.size() + " packaging records");
            
            // Set the data in the table
            packagingTable.setItems(FXCollections.observableArrayList(existingData));
            packagingTable.refresh(); // Force UI refresh
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Failed to refresh Packaging table: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }
    }
    
    private void checkPackagingTableExists() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Checking CoffeePackaging table...");
            
            // Check if the table exists
            DatabaseMetaData dbMetaData = conn.getMetaData();
            ResultSet tables = dbMetaData.getTables(null, null, "CoffeePackaging", null);
            boolean tableExists = tables.next();
            System.out.println("CoffeePackaging table exists: " + tableExists);
            
            if (tableExists) {
                // Check if there are any records
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM CoffeePackaging")) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("CoffeePackaging table has " + count + " records");
                    }
                }
                
                // Check column names
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("DESCRIBE CoffeePackaging")) {
                    System.out.println("CoffeePackaging table columns:");
                    while (rs.next()) {
                        System.out.println("- " + rs.getString("Field") + " (" + rs.getString("Type") + ")");
                    }
                } catch (SQLException e) {
                    System.out.println("Could not get column information: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking CoffeePackaging table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void insertTestPackagingData() {
        try {
            System.out.println("Inserting test packaging data...");
            
            // Get current session values
            int branchId = SessionManager.getCurrentBranchId();
            int userId = SessionManager.getCurrentUserId();
            
            if (branchId <= 0 || userId <= 0) {
                System.out.println("Invalid branch ID or user ID, using defaults");
                branchId = 1;
                userId = 1;
            }
            
            // Create test packaging records
            for (int i = 0; i < 3; i++) {
                CoffeePackaging testPackaging = new CoffeePackaging();
                testPackaging.setBranchId(branchId);
                testPackaging.setUserId(userId);
                testPackaging.setPackagingWasteKG(10.5 + i * 5);
                testPackaging.setPaCarbonEmissionsKG((10.5 + i * 5) * 6); // Calculated by DB
                testPackaging.setPackagingDate(new java.sql.Date(System.currentTimeMillis() - i * 86400000)); // Different dates
                
                boolean success = packagingDao.insert(testPackaging);
                if (success) {
                    System.out.println("Test packaging data " + (i+1) + " inserted successfully with ID: " + testPackaging.getPackagingId());
                } else {
                    System.out.println("Failed to insert test packaging data " + (i+1));
                }
            }
        } catch (Exception e) {
            System.err.println("Error inserting test packaging data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertTestPackagingDataWithSQL() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Inserting test packaging data with direct SQL...");
            
            // Check if there are already records
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM CoffeePackaging")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("CoffeePackaging table already has data, skipping direct SQL insert");
                    return;
                }
            }
            
            // Get current session values or use defaults
            int branchId = SessionManager.getCurrentBranchId();
            int userId = SessionManager.getCurrentUserId();
            
            if (branchId <= 0 || userId <= 0) {
                System.out.println("Invalid branch ID or user ID, using defaults");
                branchId = 1;
                userId = 1;
            }
            
            // Insert test data directly with SQL
            String sql = "INSERT INTO CoffeePackaging (BranchID, UserID, PackagingWaste_KG, ActivityDate) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < 3; i++) {
                    pstmt.setInt(1, branchId);
                    pstmt.setInt(2, userId);
                    pstmt.setDouble(3, 10.5 + i * 5);
                    pstmt.setDate(4, new java.sql.Date(System.currentTimeMillis() - i * 86400000));
                    
                    int rowsAffected = pstmt.executeUpdate();
                    System.out.println("Direct SQL insert " + (i+1) + " affected " + rowsAffected + " rows");
                }
            }
            
            // Verify the inserts
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM CoffeePackaging")) {
                System.out.println("CoffeePackaging records after direct SQL insert:");
                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt("PackagingID") + 
                                      ", Branch: " + rs.getInt("BranchID") + 
                                      ", User: " + rs.getInt("UserID") + 
                                      ", Waste: " + rs.getDouble("PackagingWaste_KG") + 
                                      ", Emissions: " + rs.getDouble("Pa_CarbonEmissions_KG") + 
                                      ", Date: " + rs.getDate("ActivityDate"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting test packaging data with direct SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ------ DISTRIBUTION TAB CRUD ------
    @FXML
    public void handleAddDistribution() {
        try {
            // Get current session IDs
            int branchId = SessionManager.getCurrentBranchId();
            int userId = SessionManager.getCurrentUserId();

            // Get user input
            String vehicle = vehicleTypeComboBox.getValue();
            int numVehicles = Integer.parseInt(numberOfVehiclesField.getText());
            double distPerVehicle = Double.parseDouble(distancePerVehicleField.getText());
            Date date = Date.valueOf(distributionDatePicker.getValue());

            CoffeeDistribution d = new CoffeeDistribution(
                    0, // distributionId (auto-generated)
                    branchId,
                    userId,
                    vehicle,
                    numVehicles,
                    distPerVehicle,
                    0, // totalDistance (calculated)
                    0, // fuelEfficiency (calculated)
                    0, // carbon emissions (calculated)
                    date
            );

            distributionDao.insert(d);
            handleRefreshDistribution();
            OPManagerTableUtils.clearDistributionFields(this);
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Add Distribution failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleUpdateDistribution() {
        CoffeeDistribution selected = distributionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            OPManagerTableUtils.showAlert("Please select a record to update.");
            return;
        }
        try {
            // Use session values instead of parsing labels
            selected.setBranchId(SessionManager.getCurrentBranchId());
            selected.setUserId(SessionManager.getCurrentUserId());

            selected.setVehicleType(vehicleTypeComboBox.getValue());
            selected.setNumberOfVehicles(Integer.parseInt(numberOfVehiclesField.getText()));
            selected.setDistancePerVehicleKM(Double.parseDouble(distancePerVehicleField.getText()));
            selected.setDistributionDate(Date.valueOf(distributionDatePicker.getValue()));
            distributionDao.update(selected);
            handleRefreshDistribution();
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Update Distribution failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteDistribution() {
        CoffeeDistribution selected = distributionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            OPManagerTableUtils.showAlert("Please select a record to delete.");
            return;
        }
        try {
            distributionDao.delete(selected);
            handleRefreshDistribution();
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Delete Distribution failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleSaveDistribution() {
        CoffeeDistribution selected = distributionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            OPManagerTableUtils.showAlert("Please select a distribution record to save.");
            return;
        }
        
        try {
            // Get the original record from the database
            CoffeeDistribution originalRecord = null;
            if (selected.getDistributionId() > 0) {
                originalRecord = distributionDao.getById(selected.getDistributionId());
                
                if (originalRecord == null) {
                    OPManagerTableUtils.showAlert("Could not retrieve original record.");
                    return;
                }
                
                // Keep the original user ID and branch ID to avoid foreign key issues
                selected.setUserId(originalRecord.getUserId());
            } else {
                // This is a new record, use current session values
                selected.setBranchId(SessionManager.getCurrentBranchId());
                selected.setUserId(SessionManager.getCurrentUserId());
            }
            
            // Validate inputs before setting them
            String vehicleType = vehicleTypeComboBox.getValue();
            if (vehicleType == null || vehicleType.isEmpty()) {
                OPManagerTableUtils.showAlert("Vehicle type is required.");
                return;
            }
            selected.setVehicleType(vehicleType);
            
            try {
                int numberOfVehicles = Integer.parseInt(numberOfVehiclesField.getText());
                if (numberOfVehicles <= 0) {
                    OPManagerTableUtils.showAlert("Number of vehicles must be greater than zero.");
                    return;
                }
                selected.setNumberOfVehicles(numberOfVehicles);
            } catch (NumberFormatException e) {
                OPManagerTableUtils.showAlert("Invalid number of vehicles. Please enter a valid integer.");
                return;
            }
            
            try {
                double distancePerVehicle = Double.parseDouble(distancePerVehicleField.getText());
                if (distancePerVehicle <= 0) {
                    OPManagerTableUtils.showAlert("Distance per vehicle must be greater than zero.");
                    return;
                }
                selected.setDistancePerVehicleKM(distancePerVehicle);
            } catch (NumberFormatException e) {
                OPManagerTableUtils.showAlert("Invalid distance. Please enter a valid number.");
                return;
            }
            
            if (distributionDatePicker.getValue() == null) {
                OPManagerTableUtils.showAlert("Date is required.");
                return;
            }
            selected.setDistributionDate(Date.valueOf(distributionDatePicker.getValue()));
            
            // Debug information
            System.out.println("Saving distribution with ID: " + selected.getDistributionId());
            System.out.println("UserID: " + selected.getUserId());
            System.out.println("BranchID: " + selected.getBranchId());
            
            boolean success;
            if (selected.getDistributionId() > 0) {
                // Call update directly to bypass save method
                success = distributionDao.update(selected);
            } else {
                // Call insert directly for new records
                success = distributionDao.insert(selected);
            }
            
            if (success) {
                handleRefreshDistribution();
                OPManagerTableUtils.showInfo("Distribution record saved successfully.");
            } else {
                OPManagerTableUtils.showAlert("Failed to save distribution record.");
            }
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Save Distribution failed: " + e.getMessage());
            e.printStackTrace(); // Log the stack trace for debugging
        }
    }

    @FXML
    public void handleRefreshDistribution() {
        try {
            List<CoffeeDistribution> data = distributionDao.getAll();
            distributionTable.setItems(FXCollections.observableArrayList(data));
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Failed to refresh Distribution table: " + e.getMessage());
        }
    }

    private void refreshReportBranches() {
        try {
            List<Branch> branches = branchDao.getAll();
            reportBranchTable.setItems(FXCollections.observableArrayList(branches));
        } catch (Exception e) {
            OPManagerTableUtils.showAlert("Failed to load branches for report: " + e.getMessage());
        }
    }

    @FXML
    public void handleGenerateBranchReport() {
        Branch selected = reportBranchTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            reportStatusLabel.setText("Please select a branch.");
            reportStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        try {
            CarbonFootprintMetrics metrics = fetchCarbonMetricsForBranch(selected);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PDF Report");
            fileChooser.setInitialFileName("branch_" + selected.getBranchId() + "_carbon_report.pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = fileChooser.showSaveDialog(reportStatusLabel.getScene().getWindow());
            if (file != null) {
                try (OutputStream out = new FileOutputStream(file)) {
                    reportGenerationService.generateCarbonReportPDF(metrics, out);
                }
                reportStatusLabel.setText("PDF report saved: " + file.getName());
                reportStatusLabel.setStyle("-fx-text-fill: green;");
            } else {
                reportStatusLabel.setText("Save cancelled.");
                reportStatusLabel.setStyle("-fx-text-fill: orange;");
            }
        } catch (Exception e) {
            reportStatusLabel.setText("Error: " + e.getMessage());
            reportStatusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private CarbonFootprintMetrics fetchCarbonMetricsForBranch(Branch branch) throws DataAccessException {
        CarbonFootprintMetrics metrics = new CarbonFootprintMetrics();
        metrics.setBranchId(branch.getBranchId());
        metrics.setCityName(branch.getLocation());

        double productionEmissions = productionDao.getTotalCarbonEmissionsByBranchId(branch.getBranchId());
        double packagingEmissions = packagingDao.getTotalCarbonEmissionsByBranchId(branch.getBranchId());
        double distributionEmissions = distributionDao.getTotalCarbonEmissionsByBranchId(branch.getBranchId());

        metrics.setProductionEmissions(productionEmissions);
        metrics.setPackagingEmissions(packagingEmissions);
        metrics.setDistributionEmissions(distributionEmissions);
        metrics.setTotalEmissions(productionEmissions + packagingEmissions + distributionEmissions);

        return metrics;
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}