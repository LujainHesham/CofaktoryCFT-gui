package com.cofaktory.footprint.controllers;

import com.cofaktory.footprint.dao.jdbcImpl.BranchDaoImpl;
import com.cofaktory.footprint.dao.jdbcImpl.CoffeeProductionDaoImpl;
import com.cofaktory.footprint.dao.jdbcImpl.CoffeePackagingDaoImpl;
import com.cofaktory.footprint.dao.jdbcImpl.CoffeeDistributionDaoImpl;
import com.cofaktory.footprint.dao.jdbcImpl.ReductionStrategyDaoImpl;
import com.cofaktory.footprint.model.Branch;
import com.cofaktory.footprint.model.ReductionStrategy;
import com.cofaktory.footprint.model.CarbonFootprintMetrics;
import com.cofaktory.footprint.service.ReductionStrategyService;
import com.cofaktory.footprint.service.ReportGenerationService;
import com.cofaktory.footprint.service.LanguageService;
import com.cofaktory.footprint.myExceptions.DataAccessException;
import com.cofaktory.footprint.config.DatabaseConnection;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class CioDashboardController {

    // City names & IDs mapping
    private static final Map<String, Integer> CITY_NAME_TO_ID = Map.of(
            "Cairo", 1,
            "Alexandria", 2,
            "Giza", 3,
            "Aswan", 4
    );
    private static final Map<Integer, String> CITY_ID_TO_NAME = Map.of(
            1, "Cairo",
            2, "Alexandria",
            3, "Giza",
            4, "Aswan"
    );

    // --- Cairo Tab ---
    @FXML private TableView<EmissionRow> cairoTable;
    @FXML private TableColumn<EmissionRow, Integer> cairoBranchIdCol;
    @FXML private TableColumn<EmissionRow, Double> cairoProductionCol;
    @FXML private TableColumn<EmissionRow, Double> cairoPackagingCol;
    @FXML private TableColumn<EmissionRow, Double> cairoDistributionCol;
    @FXML private TableColumn<EmissionRow, Double> cairoTotalCol;
    @FXML private Label cairoTotalLabel;
    @FXML private Label cairoPdfStatusLabel;

    // --- Alexandria Tab ---
    @FXML private TableView<EmissionRow> alexTable;
    @FXML private TableColumn<EmissionRow, Integer> alexBranchIdCol;
    @FXML private TableColumn<EmissionRow, Double> alexProductionCol;
    @FXML private TableColumn<EmissionRow, Double> alexPackagingCol;
    @FXML private TableColumn<EmissionRow, Double> alexDistributionCol;
    @FXML private TableColumn<EmissionRow, Double> alexTotalCol;
    @FXML private Label alexTotalLabel;
    @FXML private Label alexPdfStatusLabel;

    // --- Giza Tab ---
    @FXML private TableView<EmissionRow> gizaTable;
    @FXML private TableColumn<EmissionRow, Integer> gizaBranchIdCol;
    @FXML private TableColumn<EmissionRow, Double> gizaProductionCol;
    @FXML private TableColumn<EmissionRow, Double> gizaPackagingCol;
    @FXML private TableColumn<EmissionRow, Double> gizaDistributionCol;
    @FXML private TableColumn<EmissionRow, Double> gizaTotalCol;
    @FXML private Label gizaTotalLabel;
    @FXML private Label gizaPdfStatusLabel;

    // --- Aswan Tab ---
    @FXML private TableView<EmissionRow> aswanTable;
    @FXML private TableColumn<EmissionRow, Integer> aswanBranchIdCol;
    @FXML private TableColumn<EmissionRow, Double> aswanProductionCol;
    @FXML private TableColumn<EmissionRow, Double> aswanPackagingCol;
    @FXML private TableColumn<EmissionRow, Double> aswanDistributionCol;
    @FXML private TableColumn<EmissionRow, Double> aswanTotalCol;
    @FXML private Label aswanTotalLabel;
    @FXML private Label aswanPdfStatusLabel;

    // --- Reduction Plans Tab ---
    @FXML private TableView<ReductionStrategyWithStatus> reductionPlansTable;
    @FXML private TableColumn<ReductionStrategyWithStatus, Integer> rsIdColumn;
    @FXML private TableColumn<ReductionStrategyWithStatus, Integer> rsBranchIdColumn;
    @FXML private TableColumn<ReductionStrategyWithStatus, Integer> rsUserIdColumn;
    @FXML private TableColumn<ReductionStrategyWithStatus, String> rsStrategyColumn;
    @FXML private TableColumn<ReductionStrategyWithStatus, Double> rsCostsColumn;
    @FXML private TableColumn<ReductionStrategyWithStatus, Double> rsProfitsColumn;
    @FXML private TableColumn<ReductionStrategyWithStatus, String> rsStatusColumn;
    @FXML private TableColumn<ReductionStrategyWithStatus, String> rsStatusComboColumn; // Not used
    @FXML private Label reductionPlanStatusLabel;

    // --- PDF Buttons for City Tabs ---
    @FXML private Button cairoPdfButton;
    @FXML private Button alexPdfButton;
    @FXML private Button gizaPdfButton;
    @FXML private Button aswanPdfButton;

    // --- PDF Button for Reduction Plan ---
    @FXML private Button reductionPlanPdfButton;

    // Services/DAOs
    private BranchDaoImpl branchDao;
    private CoffeeProductionDaoImpl productionDao;
    private CoffeePackagingDaoImpl packagingDao;
    private CoffeeDistributionDaoImpl distributionDao;
    private ReductionStrategyService reductionStrategyService;
    private ReportGenerationService reportGenerationService;

    // Status mapping
    private static final List<String> STATUS_LIST = List.of("Pending", "Accepted", "Rejected");
    private static final Map<String, Integer> STATUS_TO_ID = Map.of(
            "Pending", 1,
            "Accepted", 2,
            "Rejected", 3
    );
    private static final Map<Integer, String> ID_TO_STATUS = Map.of(
            1, "Pending",
            2, "Accepted",
            3, "Rejected"
    );

    @FXML
    public void initialize() {
        branchDao = new BranchDaoImpl(DatabaseConnection.getDataSource());
        productionDao = new CoffeeProductionDaoImpl(DatabaseConnection.getDataSource());
        packagingDao = new CoffeePackagingDaoImpl(DatabaseConnection.getDataSource());
        distributionDao = new CoffeeDistributionDaoImpl(DatabaseConnection.getDataSource());
        reductionStrategyService = new ReductionStrategyService(new ReductionStrategyDaoImpl(DatabaseConnection.getDataSource()));
        reportGenerationService = new ReportGenerationService(new LanguageService());

        setupEmissionTables();
        loadEmissionTables();

        setupReductionPlansTable();
        loadReductionPlansTable();
    }

    // -------- City Emission Table Logic --------
    private void setupEmissionTables() {
        // Cairo
        cairoBranchIdCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getBranchId()).asObject());
        cairoProductionCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().productionEmissions).asObject());
        cairoPackagingCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().packagingEmissions).asObject());
        cairoDistributionCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().distributionEmissions).asObject());
        cairoTotalCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().totalEmissions).asObject());

        // Alexandria
        alexBranchIdCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getBranchId()).asObject());
        alexProductionCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().productionEmissions).asObject());
        alexPackagingCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().packagingEmissions).asObject());
        alexDistributionCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().distributionEmissions).asObject());
        alexTotalCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().totalEmissions).asObject());

        // Giza
        gizaBranchIdCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getBranchId()).asObject());
        gizaProductionCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().productionEmissions).asObject());
        gizaPackagingCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().packagingEmissions).asObject());
        gizaDistributionCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().distributionEmissions).asObject());
        gizaTotalCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().totalEmissions).asObject());

        // Aswan
        aswanBranchIdCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getBranchId()).asObject());
        aswanProductionCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().productionEmissions).asObject());
        aswanPackagingCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().packagingEmissions).asObject());
        aswanDistributionCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().distributionEmissions).asObject());
        aswanTotalCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().totalEmissions).asObject());
    }

    private void loadEmissionTables() {
        try {
            List<Branch> branches = branchDao.getAll();
            Map<Integer, List<Branch>> cityMap = branches.stream().collect(Collectors.groupingBy(Branch::getCityId));

            loadCityEmissionTable(cairoTable, cairoTotalLabel, cityMap.getOrDefault(CITY_NAME_TO_ID.get("Cairo"), List.of()));
            loadCityEmissionTable(alexTable, alexTotalLabel, cityMap.getOrDefault(CITY_NAME_TO_ID.get("Alexandria"), List.of()));
            loadCityEmissionTable(gizaTable, gizaTotalLabel, cityMap.getOrDefault(CITY_NAME_TO_ID.get("Giza"), List.of()));
            loadCityEmissionTable(aswanTable, aswanTotalLabel, cityMap.getOrDefault(CITY_NAME_TO_ID.get("Aswan"), List.of()));
        } catch (Exception e) {
            showAlert("Failed to load branch emissions: " + e.getMessage());
        }
    }

    private void loadCityEmissionTable(TableView<EmissionRow> table, Label totalLabel, List<Branch> branches) {
        List<EmissionRow> rows = new ArrayList<>();
        double cityTotal = 0.0;

        for (Branch branch : branches) {
            try {
                double prod = productionDao.getTotalCarbonEmissionsByBranchId(branch.getBranchId());
                double pack = packagingDao.getTotalCarbonEmissionsByBranchId(branch.getBranchId());
                double dist = distributionDao.getTotalCarbonEmissionsByBranchId(branch.getBranchId());
                double total = prod + pack + dist;
                rows.add(new EmissionRow(branch.getBranchId(), prod, pack, dist, total));
                cityTotal += total;
            } catch (DataAccessException e) {
                // Skip this branch on error
            }
        }
        table.setItems(FXCollections.observableArrayList(rows));
        totalLabel.setText("Total: " + String.format("%,.2f", cityTotal) + " kg CO₂");
    }

    public static class EmissionRow {
        private final int branchId;
        private final double productionEmissions;
        private final double packagingEmissions;
        private final double distributionEmissions;
        private final double totalEmissions;

        public EmissionRow(int branchId, double production, double packaging, double distribution, double total) {
            this.branchId = branchId;
            this.productionEmissions = production;
            this.packagingEmissions = packaging;
            this.distributionEmissions = distribution;
            this.totalEmissions = total;
        }
        public int getBranchId() { return branchId; }
    }

    // -------- Reduction Plans Tab Logic with ComboBox for Status --------
    private ObservableList<ReductionStrategyWithStatus> reductionPlansData = FXCollections.observableArrayList();

    private void setupReductionPlansTable() {
        rsIdColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getReductionId()).asObject());
        rsBranchIdColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getBranchId()).asObject());
        rsUserIdColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getUserId()).asObject());
        rsStrategyColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getReductionStrategy()));
        rsCostsColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getImplementationCosts()).asObject());
        rsProfitsColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getProjectedAnnualProfits()).asObject());
        rsStatusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());

        rsStatusColumn.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> comboBox = new ComboBox<>();

            {
                comboBox.setItems(FXCollections.observableArrayList(STATUS_LIST));
                comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                    ReductionStrategyWithStatus plan = getTableView().getItems().get(getIndex());
                    if (newVal != null && plan != null) {
                        plan.setStatus(newVal);
                    }
                });
            }

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    comboBox.setValue(status);
                    setGraphic(comboBox);
                }
            }
        });
    }

    private void loadReductionPlansTable() {
        reductionPlansData.clear();
        try {
            List<ReductionStrategy> plans = reductionStrategyService.getAllStrategies();
            for (ReductionStrategy rs : plans) {
                reductionPlansData.add(new ReductionStrategyWithStatus(rs));
            }
            reductionPlansTable.setItems(reductionPlansData);
        } catch (Exception e) {
            showAlert("Failed to load reduction plans: " + e.getMessage());
        }
    }

    @FXML
    public void handleSaveReductionPlanStatus() {
        List<ReductionStrategyWithStatus> changedPlans = new ArrayList<>();
        for (ReductionStrategyWithStatus plan : reductionPlansData) {
            if (!plan.getStatus().equals(plan.getOriginalStatus())) {
                int statusId = STATUS_TO_ID.getOrDefault(plan.getStatus(), 1);
                plan.getOriginal().setStatusId(statusId);
                try {
                    reductionStrategyService.saveStrategy(plan.getOriginal());
                    plan.setOriginalStatus(plan.getStatus());
                    changedPlans.add(plan);
                } catch (Exception e) {
                    // Optionally, collect failed updates if you want to display which failed
                }
            }
        }
        if (changedPlans.isEmpty()) {
            reductionPlanStatusLabel.setText("No statuses were changed.");
        } else {
            StringBuilder msg = new StringBuilder();
            for (ReductionStrategyWithStatus plan : changedPlans) {
                msg.append("List no. : ")
                        .append(plan.getReductionId())
                        .append(" status updated : ")
                        .append(plan.getStatus())
                        .append("\n");
            }
            reductionPlanStatusLabel.setText(msg.toString());
        }
        loadReductionPlansTable();
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

    @FXML
    public void handleGenerateReductionPlansPdf() {
        ReductionStrategyWithStatus selected = reductionPlansTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            reductionPlanStatusLabel.setText("Please select a plan to generate its report.");
            return;
        }
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Reduction Plan Report");
            fileChooser.setInitialFileName("reduction_plan_" + selected.getReductionId() + "_report.pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = fileChooser.showSaveDialog(reductionPlansTable.getScene().getWindow());
            if (file != null) {
                try (OutputStream out = new FileOutputStream(file)) {
                    reportGenerationService.generateReductionPlanReportPDF(selected.getOriginal(), out);
                }
                reductionPlanStatusLabel.setText("Reduction plan PDF report saved: " + file.getName());
            } else {
                reductionPlanStatusLabel.setText("Save cancelled.");
            }
        } catch (Exception e) {
            reductionPlanStatusLabel.setText("Report error: " + e.getMessage());
        }
    }

    // City-specific PDF generation with status label
    @FXML
    public void handleGenerateCairoEmissionsPdf() {
        handleGenerateCityEmissionsPdf("Cairo", cairoTable, cairoPdfStatusLabel);
    }
    @FXML
    public void handleGenerateAlexEmissionsPdf() {
        handleGenerateCityEmissionsPdf("Alexandria", alexTable, alexPdfStatusLabel);
    }
    @FXML
    public void handleGenerateGizaEmissionsPdf() {
        handleGenerateCityEmissionsPdf("Giza", gizaTable, gizaPdfStatusLabel);
    }
    @FXML
    public void handleGenerateAswanEmissionsPdf() {
        handleGenerateCityEmissionsPdf("Aswan", aswanTable, aswanPdfStatusLabel);
    }

    private void handleGenerateCityEmissionsPdf(String cityName, TableView<EmissionRow> cityTable, Label statusLabel) {
        try {
            List<EmissionRow> emissionRows = new ArrayList<>(cityTable.getItems());
            List<CarbonFootprintMetrics> metricsList = new ArrayList<>();
            for (EmissionRow row : emissionRows) {
                Branch branch = branchDao.getById(row.getBranchId());
                CarbonFootprintMetrics metrics = new CarbonFootprintMetrics();
                metrics.setBranchId(branch.getBranchId());
                metrics.setCityName(cityName);
                metrics.setProductionEmissions(row.productionEmissions);
                metrics.setPackagingEmissions(row.packagingEmissions);
                metrics.setDistributionEmissions(row.distributionEmissions);
                metrics.setTotalEmissions(row.totalEmissions);
                metricsList.add(metrics);
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save " + cityName + " Carbon Footprint Report");
            fileChooser.setInitialFileName(cityName.toLowerCase() + "_carbon_footprint_report.pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = fileChooser.showSaveDialog(cityTable.getScene().getWindow());
            if (file != null) {
                try (OutputStream out = new FileOutputStream(file)) {
                    reportGenerationService.generateCarbonReportPDF(metricsList, out);
                }
                statusLabel.setText("Carbon Foot Print PDF report save: " + file.getName());
            } else {
                statusLabel.setText("Save cancelled.");
            }
        } catch (Exception e) {
            statusLabel.setText("Report error: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // --- Helper class for ComboBox editable status ---
    public static class ReductionStrategyWithStatus {
        private final ReductionStrategy original;
        private final SimpleStringProperty status;
        private String originalStatus;

        public ReductionStrategyWithStatus(ReductionStrategy rs) {
            this.original = rs;
            String st = ID_TO_STATUS.getOrDefault(rs.getStatusId(), "Pending");
            this.status = new SimpleStringProperty(st);
            this.originalStatus = st;
        }

        public void setStatus(String s) { status.set(s); }
        public String getStatus() { return status.get(); }
        public StringProperty statusProperty() { return status; }

        public String getOriginalStatus() { return originalStatus; }
        public void setOriginalStatus(String s) { this.originalStatus = s; }

        public ReductionStrategy getOriginal() { return original; }
        public int getReductionId() { return original.getReductionId(); }
        public int getBranchId() { return original.getBranchId(); }
        public int getUserId() { return original.getUserId(); }
        public String getReductionStrategy() { return original.getReductionStrategy(); }
        public double getImplementationCosts() { return original.getImplementationCosts(); }
        public double getProjectedAnnualProfits() { return original.getProjectedAnnualProfits(); }
    }
}