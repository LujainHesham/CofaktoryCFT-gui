package com.cofaktory.footprint.controllers;

import com.cofaktory.footprint.dao.jdbcImpl.CoffeeProductionDaoImpl;
import com.cofaktory.footprint.dao.jdbcImpl.CoffeePackagingDaoImpl;
import com.cofaktory.footprint.dao.jdbcImpl.CoffeeDistributionDaoImpl;
import com.cofaktory.footprint.model.CoffeeProduction;
import com.cofaktory.footprint.model.CoffeePackaging;
import com.cofaktory.footprint.model.CoffeeDistribution;
import com.cofaktory.footprint.config.DatabaseConnection;
import com.cofaktory.footprint.util.WindowManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.sql.DataSource;
import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class BranchDataEntryController {

    // --- Shared ---
    @FXML private Label branchIDLabel;
    @FXML private Label userIDLabel;

    // --- Production Tab ---
    @FXML private TextField supplierField;
    @FXML private ComboBox<String> coffeeTypeComboBox;
    @FXML private ComboBox<String> productTypeComboBox;
    @FXML private TextField quantityField;
    @FXML private DatePicker productionDatePicker;
    @FXML private TableView<CoffeeProduction> productionTable;
    @FXML private TableColumn<CoffeeProduction, Integer> productionIDColumn;
    @FXML private TableColumn<CoffeeProduction, String> supplierColumn;
    @FXML private TableColumn<CoffeeProduction, String> coffeeTypeColumn;
    @FXML private TableColumn<CoffeeProduction, String> productTypeColumn;
    @FXML private TableColumn<CoffeeProduction, Double> quantityColumn;
    @FXML private TableColumn<CoffeeProduction, Date> productionDateColumn;

    // --- Packaging Tab ---
    @FXML private TextField packagingWasteField;
    @FXML private TextField packagingCO2Field;
    @FXML private DatePicker packagingDatePicker;
    @FXML private TableView<CoffeePackaging> packagingTable;
    @FXML private TableColumn<CoffeePackaging, Integer> packagingIDColumn;
    @FXML private TableColumn<CoffeePackaging, Double> packagingWasteColumn;
    @FXML private TableColumn<CoffeePackaging, Double> paCarbonEmissionsColumn;
    @FXML private TableColumn<CoffeePackaging, Date> packagingDateColumn;

    // --- Distribution Tab ---
    @FXML private ComboBox<String> vehicleTypeComboBox;
    @FXML private TextField numberOfVehiclesField;
    @FXML private TextField distancePerVehicleField;
    @FXML private TextField fuelEfficiencyField;
    @FXML private TextField distributionCO2Field;
    @FXML private DatePicker distributionDatePicker;
    @FXML private TableView<CoffeeDistribution> distributionTable;
    @FXML private TableColumn<CoffeeDistribution, Integer> distributionIDColumn;
    @FXML private TableColumn<CoffeeDistribution, String> vehicleTypeColumn;
    @FXML private TableColumn<CoffeeDistribution, Integer> numberOfVehiclesColumn;
    @FXML private TableColumn<CoffeeDistribution, Double> distancePerVehicleColumn;
    @FXML private TableColumn<CoffeeDistribution, Double> totalDistanceColumn;
    @FXML private TableColumn<CoffeeDistribution, Double> fuelEfficiencyColumn;
    @FXML private TableColumn<CoffeeDistribution, Double> vCarbonEmissionsColumn;
    @FXML private TableColumn<CoffeeDistribution, Date> distributionDateColumn;

    private final DataSource dataSource = DatabaseConnection.getDataSource();
    private final CoffeeProductionDaoImpl productionDao = new CoffeeProductionDaoImpl(dataSource);
    private final CoffeePackagingDaoImpl packagingDao = new CoffeePackagingDaoImpl(dataSource);
    private final CoffeeDistributionDaoImpl distributionDao = new CoffeeDistributionDaoImpl(dataSource);

    private int currentUserId;
    private int currentBranchId;


    @FXML private AnchorPane rootPane;

    @FXML
    public void initialize() {
        // Initialize tab components
        setupProductionTab();
        setupPackagingTab();
        setupDistributionTab();

        // Set up table selection listeners
        productionTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> populateProductionFields(selected));
        packagingTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> populatePackagingFields(selected));
        distributionTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> populateDistributionFields(selected));

        // Use Platform.runLater to maximize window after rendering
        Platform.runLater(() -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setMaximized(true);
        });
    }

    public void setUserAndBranch(int userId, int branchId) {
        this.currentUserId = userId;
        this.currentBranchId = branchId;
        branchIDLabel.setText(String.valueOf(branchId));
        userIDLabel.setText(String.valueOf(userId));
        refreshAll();
    }

    // --- Production ---
    private void setupProductionTab() {
        coffeeTypeComboBox.setItems(FXCollections.observableArrayList("Arabica Beans", "Robusta Beans", "Organic Beans"));
        productTypeComboBox.setItems(FXCollections.observableArrayList("Ground", "Whole Bean", "Instant"));
        productionIDColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getProductionId()).asObject());
        supplierColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getSupplier()));
        coffeeTypeColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCoffeeType()));
        productTypeColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getProductType()));
        quantityColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getProductionQuantitiesOfCoffeeKG()).asObject());
        productionDateColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getProductionDate()));
    }

    @FXML
    public void handleRefreshProduction() {
        refreshProduction();
    }

    private void refreshProduction() {
        try {
            // Only load the row for this branch and this user, if it exists
            List<CoffeeProduction> list = productionDao.getProductionQuantitiesByBranchId(currentBranchId);
            // If you want only the current user's row in this branch, filter by both branchId and userId here
            ObservableList<CoffeeProduction> obsList = FXCollections.observableArrayList();
            for (CoffeeProduction cp : list) {
                if (cp.getUserId() == currentUserId) {
                    obsList.add(cp);
                }
            }
            productionTable.setItems(obsList);
        } catch (Exception e) { showErrorDialog("Load Production", e.getMessage()); }
    }

    @FXML
    private void handleUpdateProduction() {
        CoffeeProduction selected = productionTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showErrorDialog("Update Production", "Select a row."); return; }
        try {
            CoffeeProduction prod = getProductionFromFields(selected.getProductionId());
            prod.setBranchId(currentBranchId);
            prod.setUserId(currentUserId);
            productionDao.update(prod);
            showInfoDialog("Production record updated.");
            refreshProduction();
            clearProductionFields();
        } catch (Exception e) {
            showErrorDialog("Update Production", e.getMessage());
        }
    }

    // --- Packaging ---
    private void setupPackagingTab() {
        packagingIDColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getPackagingId()).asObject());
        packagingWasteColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getPackagingWasteKG()).asObject());
        paCarbonEmissionsColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getPaCarbonEmissionsKG()).asObject());
        packagingDateColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getPackagingDate()));
    }

    private void refreshPackaging() {
        try {
            List<CoffeePackaging> list = packagingDao.getPackagingWasteByBranchId(currentBranchId);
            ObservableList<CoffeePackaging> obsList = FXCollections.observableArrayList();
            for (CoffeePackaging cp : list) {
                if (cp.getUserId() == currentUserId) {
                    obsList.add(cp);
                }
            }
            packagingTable.setItems(obsList);
        } catch (Exception e) { showErrorDialog("Load Packaging", e.getMessage()); }
    }

    @FXML
    private void handleUpdatePackaging() {
        CoffeePackaging selected = packagingTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showErrorDialog("Update Packaging", "Select a row."); return; }
        try {
            CoffeePackaging pack = getPackagingFromFields(selected.getPackagingId());
            pack.setBranchId(currentBranchId);
            pack.setUserId(currentUserId);
            packagingDao.update(pack);
            showInfoDialog("Packaging record updated.");
            refreshPackaging();
            clearPackagingFields();
        } catch (Exception e) {
            showErrorDialog("Update Packaging", e.getMessage());
        }
    }

    // --- Distribution ---
    private void setupDistributionTab() {
        vehicleTypeComboBox.setItems(FXCollections.observableArrayList("Minivan", "Pickup Truck"));
        distributionIDColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getDistributionId()).asObject());
        vehicleTypeColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getVehicleType()));
        numberOfVehiclesColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getNumberOfVehicles()).asObject());
        distancePerVehicleColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getDistancePerVehicleKM()).asObject());
        totalDistanceColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getTotalDistanceKM()).asObject());
        fuelEfficiencyColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getFuelEfficiency()).asObject());
        vCarbonEmissionsColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getVCarbonEmissionsKg()).asObject());
        distributionDateColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getDistributionDate()));
    }

    private void refreshDistribution() {
        try {
            List<CoffeeDistribution> list = distributionDao.getDistributionsByBranchId(currentBranchId);
            ObservableList<CoffeeDistribution> obsList = FXCollections.observableArrayList();
            for (CoffeeDistribution cd : list) {
                if (cd.getUserId() == currentUserId) {
                    obsList.add(cd);
                }
            }
            distributionTable.setItems(obsList);
        } catch (Exception e) { showErrorDialog("Load Distribution", e.getMessage()); }
    }



    @FXML
    private void handleUpdateDistribution() {
        CoffeeDistribution selected = distributionTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showErrorDialog("Update Distribution", "Select a row."); return; }
        try {
            CoffeeDistribution dist = getDistributionFromFields(selected.getDistributionId());
            dist.setBranchId(currentBranchId);
            dist.setUserId(currentUserId);
            distributionDao.update(dist);
            showInfoDialog("Distribution record updated.");
            refreshDistribution();
            clearDistributionFields();
        } catch (Exception e) {
            showErrorDialog("Update Distribution", e.getMessage());
        }
    }

    // --- Getters from Fields ---
    private CoffeeProduction getProductionFromFields(int id) {
        String supplier = supplierField.getText();
        String coffeeType = coffeeTypeComboBox.getValue();
        String productType = productTypeComboBox.getValue();
        double qty = Double.parseDouble(quantityField.getText());
        Date date = Date.valueOf(productionDatePicker.getValue());
        return new CoffeeProduction(id, currentBranchId, currentUserId, supplier, coffeeType, productType, qty, 0.0, date);
    }
    private void populateProductionFields(CoffeeProduction prod) {
        if (prod == null) return;
        supplierField.setText(prod.getSupplier());
        coffeeTypeComboBox.setValue(prod.getCoffeeType());
        productTypeComboBox.setValue(prod.getProductType());
        quantityField.setText(String.valueOf(prod.getProductionQuantitiesOfCoffeeKG()));
        productionDatePicker.setValue(prod.getProductionDate() != null ? prod.getProductionDate().toLocalDate() : null);
    }
    private void clearProductionFields() {
        supplierField.clear(); coffeeTypeComboBox.setValue(null); productTypeComboBox.setValue(null);
        quantityField.clear(); productionDatePicker.setValue(null);
    }

    private CoffeePackaging getPackagingFromFields(int id) {
        double waste = Double.parseDouble(packagingWasteField.getText());
        Date date = Date.valueOf(packagingDatePicker.getValue());
        return new CoffeePackaging(id, currentBranchId, currentUserId, waste, 0.0, date);
    }
    private void populatePackagingFields(CoffeePackaging pack) {
        if (pack == null) return;
        packagingWasteField.setText(String.valueOf(pack.getPackagingWasteKG()));
        packagingDatePicker.setValue(pack.getPackagingDate() != null ? pack.getPackagingDate().toLocalDate() : null);
    }
    private void clearPackagingFields() {
        packagingWasteField.clear();
        packagingDatePicker.setValue(null);
    }

    private CoffeeDistribution getDistributionFromFields(int id) {
        String vehicleType = vehicleTypeComboBox.getValue();
        int numVehicles = Integer.parseInt(numberOfVehiclesField.getText());
        double distance = Double.parseDouble(distancePerVehicleField.getText());
        Date date = Date.valueOf(distributionDatePicker.getValue());
        return new CoffeeDistribution(id, currentBranchId, currentUserId, vehicleType, numVehicles, distance, 0.0, 0.0, 0.0, date);
    }
    private void populateDistributionFields(CoffeeDistribution dist) {
        if (dist == null) return;
        vehicleTypeComboBox.setValue(dist.getVehicleType());
        numberOfVehiclesField.setText(String.valueOf(dist.getNumberOfVehicles()));
        distancePerVehicleField.setText(String.valueOf(dist.getDistancePerVehicleKM()));
        distributionDatePicker.setValue(dist.getDistributionDate() != null ? dist.getDistributionDate().toLocalDate() : null);
    }
    private void clearDistributionFields() {
        vehicleTypeComboBox.setValue(null);
        numberOfVehiclesField.clear();
        distancePerVehicleField.clear();
        distributionDatePicker.setValue(null);
    }

    // --- Shared ---
    private void showErrorDialog(String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    private void showInfoDialog(String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info");
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void refreshAll() {
        refreshProduction();
        refreshPackaging();
        refreshDistribution();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Create a new stage for the login screen
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Close the current stage first
            currentStage.close();

            // Create new stage for login
            Stage loginStage = new Stage();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/login.fxml")));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/views/styles.css").toExternalForm());

            loginStage.setScene(scene);
            loginStage.setTitle("Carbon Footprint Tracker - Login");

            // Set window size before showing (avoid resize flickering)
            loginStage.setWidth(1024);
            loginStage.setHeight(768);

            loginStage.show();

            // Maximize after showing to prevent flickering
            Platform.runLater(() -> {
                loginStage.setMaximized(true);
            });
        } catch (Exception e) {
            e.printStackTrace();
            // Show error to user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Logout Failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }


    public void handleRefreshDistribution(ActionEvent actionEvent) {
        refreshDistribution();
    }
    public void handleRefreshProduction(ActionEvent actionEvent) {
        refreshProduction();
    }
    public void handleRefreshPackaging(ActionEvent actionEvent) {
        refreshPackaging();
    }
    public void handleRefreshAll(ActionEvent actionEvent) {
        refreshAll();
    }


}