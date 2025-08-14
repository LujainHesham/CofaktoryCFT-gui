package com.cofaktory.footprint.controllers;

import com.cofaktory.footprint.dao.jdbcImpl.BranchDaoImpl;
import com.cofaktory.footprint.dao.jdbcImpl.CoffeeProductionDaoImpl;
import com.cofaktory.footprint.dao.jdbcImpl.CoffeePackagingDaoImpl;
import com.cofaktory.footprint.dao.jdbcImpl.CoffeeDistributionDaoImpl;
import com.cofaktory.footprint.dao.jdbcImpl.ReductionStrategyDaoImpl;
import com.cofaktory.footprint.model.Branch;
import com.cofaktory.footprint.model.ReductionStrategy;
import com.cofaktory.footprint.service.BranchService;
import com.cofaktory.footprint.service.ReductionStrategyService;
import com.cofaktory.footprint.myExceptions.DataAccessException;
import com.cofaktory.footprint.config.DatabaseConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javax.sql.DataSource;
import javafx.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CeoDashboardController {

    // Emissions Overview
    @FXML
    private LineChart<String, Number> emissionsOverTimeChart;
    @FXML
    private BarChart<String, Number> emissionsByCityChart;
    @FXML
    private PieChart emissionsByProcessChart;
    @FXML
    private Label emissionsOverTimeDesc;
    @FXML
    private Label emissionsByCityDesc;
    @FXML
    private Label emissionsByProcessDesc;

    // Reduction Plans & Impact
    @FXML
    private PieChart reductionPlansStatusChart;
    @FXML
    private Label reductionPlansStatusDesc;
    @FXML
    private BarChart<String, Number> projectedProfitsChart;
    @FXML
    private Label projectedProfitsDesc;
    @FXML
    private BarChart<String, Number> implementationCostsChart;
    @FXML
    private Label implementationCostsDesc;

    // Operations & Performance
    @FXML
    private BarChart<String, Number> emissionsPerBranchChart;
    @FXML
    private Label emissionsPerBranchDesc;
    @FXML
    private BarChart<String, Number> emissionsPerEmployeeChart;
    @FXML
    private Label emissionsPerEmployeeDesc;
    @FXML
    private BarChart<String, Number> topWorstBranchesChart;
    @FXML
    private Label topWorstBranchesDesc;
    @FXML
    private BarChart<String, Number> topBestBranchesChart;
    @FXML
    private Label topBestBranchesDesc;

    // Data access
    private final BranchService branchService;
    private final ReductionStrategyService reductionStrategyService;
    private final CoffeeProductionDaoImpl productionDao;
    private final CoffeePackagingDaoImpl packagingDao;
    private final CoffeeDistributionDaoImpl distributionDao;

    public CeoDashboardController() {
        DataSource ds = DatabaseConnection.getDataSource();
        this.branchService = new BranchService(new BranchDaoImpl(ds));
        this.reductionStrategyService = new ReductionStrategyService(new ReductionStrategyDaoImpl(ds));
        this.productionDao = new CoffeeProductionDaoImpl(ds);
        this.packagingDao = new CoffeePackagingDaoImpl(ds);
        this.distributionDao = new CoffeeDistributionDaoImpl(ds);
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            setupAllCharts(); // Apply global chart settings first
            loadAllCharts(); // Then load data
        });
    }

    /**
     * Apply global chart settings before loading data
     */
    private void setupAllCharts() {
        // Set up all bar charts
        setupBarChart(emissionsByCityChart);
        setupBarChart(projectedProfitsChart);
        setupBarChart(implementationCostsChart);
        setupBarChart(emissionsPerBranchChart);
        setupBarChart(emissionsPerEmployeeChart);
        setupBarChart(topWorstBranchesChart);
        setupBarChart(topBestBranchesChart);

        // Set up line chart
        setupLineChart(emissionsOverTimeChart);

        // Set up pie charts (legend positioning)
        setupPieChart(emissionsByProcessChart);
        setupPieChart(reductionPlansStatusChart);
    }

    /**
     * Configure bar chart display settings
     */
    private void setupBarChart(BarChart<String, Number> chart) {
        if (chart == null) return;

        // Make sure X-axis is a CategoryAxis
        if (chart.getXAxis() instanceof CategoryAxis) {
            CategoryAxis xAxis = (CategoryAxis) chart.getXAxis();

            // Set rotation for labels
            xAxis.setTickLabelRotation(45); // More rotation angle (45 degrees)

            // Increase spacing between labels
            xAxis.setTickLabelGap(10);

            // Reduce font size of labels
            xAxis.tickLabelFontProperty().set(Font.font(9));

            // Adjust category spacing
            chart.setCategoryGap(20);
            chart.setBarGap(2);
        }

        // General chart settings
        chart.setAnimated(false); // Disable animations for better performance
        chart.setLegendVisible(false); // Hide legend if not needed
        chart.setHorizontalGridLinesVisible(true);
    }

    /**
     * Configure line chart display settings
     */
    private void setupLineChart(LineChart<String, Number> chart) {
        if (chart == null) return;

        // Make sure X-axis is a CategoryAxis
        if (chart.getXAxis() instanceof CategoryAxis) {
            CategoryAxis xAxis = (CategoryAxis) chart.getXAxis();

            // Set rotation for labels
            xAxis.setTickLabelRotation(45); // More rotation angle

            // Increase spacing between labels
            xAxis.setTickLabelGap(10);

            // Reduce font size of labels
            xAxis.tickLabelFontProperty().set(Font.font(9));

            // Set some horizontal padding
            xAxis.setStartMargin(10);
            xAxis.setEndMargin(10);
        }

        // General chart settings
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setHorizontalGridLinesVisible(true);
    }

    /**
     * Configure pie chart display settings
     */
    private void setupPieChart(PieChart chart) {
        if (chart == null) return;

        // Move legend to the side
        chart.setLegendSide(Side.RIGHT);
        chart.setLabelsVisible(true);
        chart.setAnimated(false);
    }

    private void loadAllCharts() {
        try {
            List<Branch> branches = branchService.getAllBranches();
            List<ReductionStrategy> plans = reductionStrategyService.getAllStrategies();

            loadEmissionsOverTimeChart(branches);
            loadEmissionsByCityChart(branches);
            loadEmissionsByProcessChart(branches);
            loadReductionPlansStatusChart(plans);
            loadProjectedProfitsChart(plans, branches);
            loadImplementationCostsChart(plans, branches);
            loadEmissionsPerBranchChart(branches);
            loadEmissionsPerEmployeeChart(branches);
            loadTopWorstBranchesChart(branches);
            loadTopBestBranchesChart(branches);

        } catch (DataAccessException e) {
            setAllDescriptions("Data loading error: " + e.getMessage());
        }
    }

    // Chart Loaders

    private void loadEmissionsOverTimeChart(List<Branch> branches) {
        try {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Total Emissions");

            Map<String, Double> monthTotals = new LinkedHashMap<>();
            LocalDate now = LocalDate.now();
            for (int i = 5; i >= 0; i--) {
                // Use shorter month format to reduce label length
                String month = now.minusMonths(i).format(DateTimeFormatter.ofPattern("MM/yy"));
                monthTotals.put(month, 0.0);
            }

            // Approximate: evenly distribute total emissions across months
            for (Branch branch : branches) {
                double total = getBranchEmissions(branch);
                int months = monthTotals.size();
                double perMonth = months > 0 ? total / months : 0.0;
                for (String month : monthTotals.keySet()) {
                    monthTotals.put(month, monthTotals.get(month) + perMonth);
                }
            }
            emissionsOverTimeChart.getData().clear();
            for (Map.Entry<String, Double> entry : monthTotals.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            emissionsOverTimeChart.getData().add(series);
            emissionsOverTimeDesc.setText("Shows the trend of total company emissions over the last 6 months. Use this to track progress in reducing overall emissions.");
        } catch (Exception e) {
            emissionsOverTimeDesc.setText("Error loading emissions over time: " + e.getMessage());
        }
    }

    private void loadEmissionsByCityChart(List<Branch> branches) {
        try {
            Map<String, Double> cityTotals = new HashMap<>();
            for (Branch b : branches) {
                String city = cityName(b.getCityId());
                cityTotals.put(city, cityTotals.getOrDefault(city, 0.0) + getBranchEmissions(b));
            }

            // Sort the data for better visualization
            List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(cityTotals.entrySet());
            sortedEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (Map.Entry<String, Double> entry : sortedEntries) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            emissionsByCityChart.getData().clear();
            emissionsByCityChart.getData().add(series);

            emissionsByCityDesc.setText("Compares total emissions across cities. Identify which city has the highest carbon footprint.");
        } catch (Exception e) {
            emissionsByCityDesc.setText("Error loading emissions by city: " + e.getMessage());
        }
    }

    private void loadEmissionsByProcessChart(List<Branch> branches) {
        try {
            double totalProd = 0, totalPack = 0, totalDist = 0;
            for (Branch b : branches) {
                int branchId = b.getBranchId();
                totalProd += safe(productionDao.getTotalCarbonEmissionsByBranchId(branchId));
                totalPack += safe(packagingDao.getTotalCarbonEmissionsByBranchId(branchId));
                totalDist += safe(distributionDao.getTotalCarbonEmissionsByBranchId(branchId));
            }
            emissionsByProcessChart.getData().clear();
            emissionsByProcessChart.getData().addAll(
                    new PieChart.Data("Production", totalProd),
                    new PieChart.Data("Packaging", totalPack),
                    new PieChart.Data("Distribution", totalDist)
            );
            emissionsByProcessDesc.setText("Breakdown of emissions by coffee process. Focus reduction efforts on the largest segment.");
        } catch (Exception e) {
            emissionsByProcessDesc.setText("Error loading emissions by process: " + e.getMessage());
        }
    }

    private void loadReductionPlansStatusChart(List<ReductionStrategy> plans) {
        try {
            Map<String, Long> counts = plans.stream().collect(Collectors.groupingBy(
                    this::statusName, Collectors.counting()));
            reductionPlansStatusChart.getData().clear();
            for (Map.Entry<String, Long> entry : counts.entrySet()) {
                reductionPlansStatusChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            reductionPlansStatusDesc.setText("Shows the proportion of reduction plans by status (Accepted, Pending, Rejected).");
        } catch (Exception e) {
            reductionPlansStatusDesc.setText("Error loading reduction plans status: " + e.getMessage());
        }
    }

    private void loadProjectedProfitsChart(List<ReductionStrategy> plans, List<Branch> branches) {
        try {
            Map<Integer, String> branchIdToCity = branches.stream().collect(Collectors.toMap(
                    Branch::getBranchId, b -> cityName(b.getCityId()), (a, b) -> a));
            Map<String, Double> cityProfits = new HashMap<>();
            for (ReductionStrategy plan : plans) {
                String city = branchIdToCity.getOrDefault(plan.getBranchId(), "Unknown");
                cityProfits.put(city, cityProfits.getOrDefault(city, 0.0) + plan.getProjectedAnnualProfits());
            }

            // Sort the data for better visualization
            List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(cityProfits.entrySet());
            sortedEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (Map.Entry<String, Double> entry : sortedEntries) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            projectedProfitsChart.getData().clear();
            projectedProfitsChart.getData().add(series);

            projectedProfitsDesc.setText("Projected annual profits from all accepted reduction plans, grouped by city.");
        } catch (Exception e) {
            projectedProfitsDesc.setText("Error loading projected profits: " + e.getMessage());
        }
    }

    private void loadImplementationCostsChart(List<ReductionStrategy> plans, List<Branch> branches) {
        try {
            Map<Integer, String> branchIdToCity = branches.stream().collect(Collectors.toMap(
                    Branch::getBranchId, b -> cityName(b.getCityId()), (a, b) -> a));
            Map<String, Double> cityCosts = new HashMap<>();
            for (ReductionStrategy plan : plans) {
                String city = branchIdToCity.getOrDefault(plan.getBranchId(), "Unknown");
                cityCosts.put(city, cityCosts.getOrDefault(city, 0.0) + plan.getImplementationCosts());
            }

            // Sort the data for better visualization
            List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(cityCosts.entrySet());
            sortedEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (Map.Entry<String, Double> entry : sortedEntries) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            implementationCostsChart.getData().clear();
            implementationCostsChart.getData().add(series);

            implementationCostsDesc.setText("Total implementation costs for reduction plans, grouped by city.");
        } catch (Exception e) {
            implementationCostsDesc.setText("Error loading implementation costs: " + e.getMessage());
        }
    }

    private void loadEmissionsPerBranchChart(List<Branch> branches) {
        try {
            // For branch charts, use abbreviated labels to reduce overlap
            XYChart.Series<String, Number> series = new XYChart.Series<>();

            // Limit to fewer branches if there are many
            List<Branch> branchesToShow = branches;
            if (branches.size() > 8) {
                branchesToShow = branches.subList(0, 8);
            }

            for (Branch b : branchesToShow) {
                series.getData().add(new XYChart.Data<>("B" + b.getBranchId(), getBranchEmissions(b)));
            }

            emissionsPerBranchChart.getData().clear();
            emissionsPerBranchChart.getData().add(series);

            emissionsPerBranchDesc.setText("Total emissions per branch. Identify high and low performing branches.");
        } catch (Exception e) {
            emissionsPerBranchDesc.setText("Error loading emissions per branch: " + e.getMessage());
        }
    }

    private void loadEmissionsPerEmployeeChart(List<Branch> branches) {
        try {
            // For branch charts, use abbreviated labels to reduce overlap
            XYChart.Series<String, Number> series = new XYChart.Series<>();

            // Limit to fewer branches if there are many
            List<Branch> branchesToShow = branches;
            if (branches.size() > 8) {
                branchesToShow = branches.subList(0, 8);
            }

            for (Branch b : branchesToShow) {
                double emissions = getBranchEmissions(b);
                double perEmployee = (b.getNumberOfEmployees() > 0) ? emissions / b.getNumberOfEmployees() : 0;
                series.getData().add(new XYChart.Data<>("B" + b.getBranchId(), perEmployee));
            }

            emissionsPerEmployeeChart.getData().clear();
            emissionsPerEmployeeChart.getData().add(series);

            emissionsPerEmployeeDesc.setText("Emissions per employee for each branch. Normalizes performance by staff size.");
        } catch (Exception e) {
            emissionsPerEmployeeDesc.setText("Error loading emissions per employee: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/login.fxml")));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTopWorstBranchesChart(List<Branch> branches) {
        try {
            List<Branch> sorted = new ArrayList<>(branches);
            sorted.sort(Comparator.comparingDouble(this::getBranchEmissions).reversed());
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            int count = 0;
            for (Branch b : sorted) {
                if (count++ >= 5) break;
                series.getData().add(new XYChart.Data<>("B" + b.getBranchId(), getBranchEmissions(b)));
            }
            topWorstBranchesChart.getData().clear();
            topWorstBranchesChart.getData().add(series);

            topWorstBranchesDesc.setText("Top 5 branches with the highest total emissions.");
        } catch (Exception e) {
            topWorstBranchesDesc.setText("Error loading top worst branches: " + e.getMessage());
        }
    }

    private void loadTopBestBranchesChart(List<Branch> branches) {
        try {
            List<Branch> sorted = new ArrayList<>(branches);
            sorted.sort(Comparator.comparingDouble(this::getBranchEmissions));
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            int count = 0;
            for (Branch b : sorted) {
                if (count++ >= 5) break;
                series.getData().add(new XYChart.Data<>("B" + b.getBranchId(), getBranchEmissions(b)));
            }
            topBestBranchesChart.getData().clear();
            topBestBranchesChart.getData().add(series);

            topBestBranchesDesc.setText("Top 5 branches with the lowest total emissions.");
        } catch (Exception e) {
            topBestBranchesDesc.setText("Error loading top best branches: " + e.getMessage());
        }
    }

    // Data Helpers

    private static String cityName(int cityId) {
        switch (cityId) {
            case 1:
                return "Cairo";
            case 2:
                return "Alex";  // Abbreviated city names
            case 3:
                return "Giza";
            case 4:
                return "Aswan";
            default:
                return "Other";
        }
    }

    private String statusName(ReductionStrategy plan) {
        int statusId = plan.getStatusId();
        switch (statusId) {
            case 1:
                return "Pending";
            case 2:
                return "Accepted";
            case 3:
                return "Rejected";
            default:
                return "Other";
        }
    }

    private double getBranchEmissions(Branch branch) {
        try {
            double prod = safe(productionDao.getTotalCarbonEmissionsByBranchId(branch.getBranchId()));
            double pack = safe(packagingDao.getTotalCarbonEmissionsByBranchId(branch.getBranchId()));
            double dist = safe(distributionDao.getTotalCarbonEmissionsByBranchId(branch.getBranchId()));
            return prod + pack + dist;
        } catch (DataAccessException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private double safe(Double d) {
        return d == null ? 0.0 : d;
    }

    private void setAllDescriptions(String msg) {
        emissionsOverTimeDesc.setText(msg);
        emissionsByCityDesc.setText(msg);
        emissionsByProcessDesc.setText(msg);
        reductionPlansStatusDesc.setText(msg);
        projectedProfitsDesc.setText(msg);
        implementationCostsDesc.setText(msg);
        emissionsPerBranchDesc.setText(msg);
        emissionsPerEmployeeDesc.setText(msg);
        topWorstBranchesDesc.setText(msg);
        topBestBranchesDesc.setText(msg);
    }
}