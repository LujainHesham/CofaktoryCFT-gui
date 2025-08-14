package com.cofaktory.footprint.service;

import com.cofaktory.footprint.dao.*;
import com.cofaktory.footprint.model.*;
import com.cofaktory.footprint.myExceptions.DataAccessException;
import com.cofaktory.footprint.myExceptions.DataImportException;
import org.apache.commons.csv.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ImportExportService {
    private final CoffeeProductionDao productionDao;
    private final CoffeePackagingDao packagingDao;
    private final CoffeeDistributionDao distributionDao;
    private final ReductionStrategyDao reductionStrategyDao;
    private final BranchDao branchDao;
    private final AuditLoggingDao auditLoggingDao;

    public ImportExportService(
            CoffeeProductionDao productionDao,
            CoffeePackagingDao packagingDao,
            CoffeeDistributionDao distributionDao,
            ReductionStrategyDao reductionStrategyDao,
            BranchDao branchDao,
            AuditLoggingDao auditLoggingDao) {
        this.productionDao = productionDao;
        this.packagingDao = packagingDao;
        this.distributionDao = distributionDao;
        this.reductionStrategyDao = reductionStrategyDao;
        this.branchDao = branchDao;
        this.auditLoggingDao = auditLoggingDao;
    }

    // PRODUCTION DATA METHODS

    // Import CSV and save to database
    public void importAndSaveProductionData(InputStream inputStream, int userId)
            throws DataImportException, DataAccessException {
        List<CoffeeProduction> productions = importProductionDataFromCSV(inputStream);

        for (CoffeeProduction production : productions) {
            boolean success = productionDao.save(production);
            if (success) {
                logAction(userId, "INSERT", "CoffeeProduction", production.getProductionId());
            }
        }
    }

    // Export data from database to CSV
    public void exportProductionDataToCSV(OutputStream outputStream, int branchId)
            throws DataImportException, DataAccessException {
        List<CoffeeProduction> productions;
        if (branchId > 0) {
            productions = productionDao.getProductionQuantitiesByBranchId(branchId);
        } else {
            productions = productionDao.getAll();
        }

        exportProductionDataToCSV(productions, outputStream);
    }

    // Original CSV parsing method (unchanged)
    public List<CoffeeProduction> importProductionDataFromCSV(InputStream inputStream) throws DataImportException {
        List<CoffeeProduction> productions = new ArrayList<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, format)){

            for (CSVRecord record : parser) {
                CoffeeProduction production = new CoffeeProduction();
                production.setBranchId(Integer.parseInt(record.get("BranchID")));
                production.setUserId(Integer.parseInt(record.get("UserID")));
                production.setSupplier(record.get("Supplier"));
                production.setCoffeeType(record.get("CoffeeType"));
                production.setProductType(record.get("ProductType"));
                production.setProductionQuantitiesOfCoffeeKG(Double.parseDouble(record.get("ProductionQuantitiesKG")));
                production.setPrCarbonEmissionsKG(Double.parseDouble(record.get("CarbonEmissionsKG")));
                productions.add(production);
            }
        } catch (Exception e) {
            throw new DataImportException("Failed to import production data", e);
        }
        return productions;
    }

    // Original CSV export method (unchanged)
    public void exportProductionDataToCSV(List<CoffeeProduction> productions, OutputStream outputStream)
            throws DataImportException {

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("ProductionID", "BranchID", "UserID", "Supplier", "CoffeeType",
                        "ProductType", "ProductionQuantitiesKG", "CarbonEmissionsKG")
                .build();

        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, format)) {

            for (CoffeeProduction production : productions) {
                printer.printRecord(
                        production.getProductionId(),
                        production.getBranchId(),
                        production.getUserId(),
                        production.getSupplier(),
                        production.getCoffeeType(),
                        production.getProductType(),
                        production.getProductionQuantitiesOfCoffeeKG(),
                        production.getPrCarbonEmissionsKG()
                );
            }
        } catch (Exception e) {
            throw new DataImportException("Failed to export production data", e);
        }
    }

    // PACKAGING DATA METHODS

    // Import CSV and save to database
    public void importAndSavePackagingData(InputStream inputStream, int userId)
            throws DataImportException, DataAccessException {
        List<CoffeePackaging> packagingList = importPackagingDataFromCSV(inputStream);

        for (CoffeePackaging packaging : packagingList) {
            boolean success = packagingDao.save(packaging);
            if (success) {
                logAction(userId, "INSERT", "CoffeePackaging", packaging.getPackagingId());
            }
        }
    }

    // Export data from database to CSV
    public void exportPackagingDataToCSV(OutputStream outputStream, int branchId)
            throws DataImportException, DataAccessException {
        List<CoffeePackaging> packagingList;
        if (branchId > 0) {
            packagingList = packagingDao.getPackagingWasteByBranchId(branchId);
        } else {
            packagingList = packagingDao.getAll();
        }

        exportPackagingDataToCSV(packagingList, outputStream);
    }

    // Original CSV parsing method (unchanged)
    public List<CoffeePackaging> importPackagingDataFromCSV(InputStream inputStream) throws DataImportException {
        List<CoffeePackaging> packagingList = new ArrayList<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader,format)) {

            for (CSVRecord record : parser) {
                CoffeePackaging packaging = new CoffeePackaging();
                packaging.setPackagingId(Integer.parseInt(record.get("PackagingID")));
                packaging.setBranchId(Integer.parseInt(record.get("BranchID")));
                packaging.setUserId(Integer.parseInt(record.get("UserID")));
                packaging.setPackagingWasteKG(Double.parseDouble(record.get("PackagingWasteKG")));
                packaging.setPaCarbonEmissionsKG(Double.parseDouble(record.get("CarbonEmissionsKG")));
                packagingList.add(packaging);
            }
        } catch (Exception e) {
            throw new DataImportException("Failed to import packaging data", e);
        }
        return packagingList;
    }

    // Original CSV export method (unchanged)
    public void exportPackagingDataToCSV(List<CoffeePackaging> packagingList, OutputStream outputStream)
            throws DataImportException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("PackagingID", "BranchID", "UserID", "PackagingWasteKG", "CarbonEmissionsKG")
                .build();

        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, format)) {

            for (CoffeePackaging packaging : packagingList) {
                printer.printRecord(
                        packaging.getPackagingId(),
                        packaging.getBranchId(),
                        packaging.getUserId(),
                        packaging.getPackagingWasteKG(),
                        packaging.getPaCarbonEmissionsKG()
                );
            }
        } catch (Exception e) {
            throw new DataImportException("Failed to export packaging data", e);
        }
    }

    // DISTRIBUTION DATA METHODS

    // Import CSV and save to database
    public void importAndSaveDistributionData(InputStream inputStream, int userId)
            throws DataImportException, DataAccessException {
        List<CoffeeDistribution> distributions = importDistributionDataFromCSV(inputStream);

        for (CoffeeDistribution distribution : distributions) {
            boolean success = distributionDao.save(distribution);
            if (success) {
                logAction(userId, "INSERT", "CoffeeDistribution", distribution.getDistributionId());
            }
        }
    }

    // Export data from database to CSV
    public void exportDistributionDataToCSV(OutputStream outputStream, int branchId)
            throws DataImportException, DataAccessException {
        List<CoffeeDistribution> distributions;
        if (branchId > 0) {
            distributions = distributionDao.getDistributionsByBranchId(branchId);
        } else {
            distributions = distributionDao.getAll();
        }

        exportDistributionDataToCSV(distributions, outputStream);
    }

    // Original CSV parsing method (unchanged)
    public List<CoffeeDistribution> importDistributionDataFromCSV(InputStream inputStream) throws DataImportException {
        List<CoffeeDistribution> distributions = new ArrayList<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader,format)) {

            for (CSVRecord record : parser) {
                CoffeeDistribution distribution = new CoffeeDistribution();
                distribution.setDistributionId(Integer.parseInt(record.get("DistributionID")));
                distribution.setBranchId(Integer.parseInt(record.get("BranchID")));
                distribution.setUserId(Integer.parseInt(record.get("UserID")));
                distribution.setVehicleType(record.get("VehicleType"));
                distribution.setNumberOfVehicles(Integer.parseInt(record.get("NumberOfVehicles")));
                distribution.setDistancePerVehicleKM(Double.parseDouble(record.get("DistancePerVehicleKM")));
                distribution.setFuelEfficiency(Double.parseDouble(record.get("FuelEfficiency")));
                distribution.setVCarbonEmissionsKg(Double.parseDouble(record.get("CarbonEmissionsKg")));
                distributions.add(distribution);
            }
        } catch (Exception e) {
            throw new DataImportException("Failed to import distribution data", e);
        }
        return distributions;
    }

    // Original CSV export method (unchanged)
    public void exportDistributionDataToCSV(List<CoffeeDistribution> distributions, OutputStream outputStream)
            throws DataImportException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("DistributionID", "BranchID", "UserID", "VehicleType", "NumberOfVehicles",
                        "DistancePerVehicleKM", "FuelEfficiency", "CarbonEmissionsKg")
                .build();
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, format)) {

            for (CoffeeDistribution distribution : distributions) {
                printer.printRecord(
                        distribution.getDistributionId(),
                        distribution.getBranchId(),
                        distribution.getUserId(),
                        distribution.getVehicleType(),
                        distribution.getNumberOfVehicles(),
                        distribution.getDistancePerVehicleKM(),
                        distribution.getFuelEfficiency(),
                        distribution.getVCarbonEmissionsKg()
                );
            }
        } catch (Exception e) {
            throw new DataImportException("Failed to export distribution data", e);
        }
    }

    // REDUCTION STRATEGY METHODS

    // Import CSV and save to database
    public void importAndSaveStrategies(InputStream inputStream, int userId)
            throws DataImportException, DataAccessException {
        List<ReductionStrategy> strategies = importStrategiesFromCSV(inputStream);

        for (ReductionStrategy strategy : strategies) {
            boolean success = reductionStrategyDao.save(strategy);
            if (success) {
                logAction(userId, "INSERT", "ReductionStrategy", strategy.getReductionId());
            }
        }
    }

    // Export data from database to CSV
    public void exportStrategiesToCSV(OutputStream outputStream, int branchId)
            throws DataImportException, DataAccessException {
        List<ReductionStrategy> strategies;
        if (branchId > 0) {
            strategies = reductionStrategyDao.getReductionStrategiesByBranchId(branchId);
        } else {
            strategies = reductionStrategyDao.getAll();
        }

        exportStrategiesToCSV(strategies, outputStream);
    }

    // Original CSV parsing method (unchanged)
    public List<ReductionStrategy> importStrategiesFromCSV(InputStream inputStream) throws DataImportException {
        List<ReductionStrategy> strategies = new ArrayList<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader,format)) {

            for (CSVRecord record : parser) {
                ReductionStrategy strategy = new ReductionStrategy();
                strategy.setReductionId(Integer.parseInt(record.get("ReductionID")));
                strategy.setBranchId(Integer.parseInt(record.get("BranchID")));
                strategy.setUserId(Integer.parseInt(record.get("UserID")));
                strategy.setReductionStrategy(record.get("ReductionStrategy"));
                strategy.setStatusId(Integer.parseInt(record.get("StatusID")));
                strategy.setImplementationCosts(Double.parseDouble(record.get("ImplementationCosts")));
                strategy.setProjectedAnnualProfits(Double.parseDouble(record.get("ProjectedAnnualProfits")));
                strategies.add(strategy);
            }
        } catch (Exception e) {
            throw new DataImportException("Failed to import reduction strategies", e);
        }
        return strategies;
    }

    // Original CSV export method (unchanged)
    public void exportStrategiesToCSV(List<ReductionStrategy> strategies, OutputStream outputStream)
            throws DataImportException {

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("ReductionID", "BranchID", "UserID", "ReductionStrategy", "StatusID",
                        "ImplementationCosts", "ProjectedAnnualProfits")
                .build();

        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, format)) {

            for (ReductionStrategy strategy : strategies) {
                printer.printRecord(
                        strategy.getReductionId(),
                        strategy.getBranchId(),
                        strategy.getUserId(),
                        strategy.getReductionStrategy(),
                        strategy.getStatusId(),
                        strategy.getImplementationCosts(),
                        strategy.getProjectedAnnualProfits()
                );
            }
        } catch (Exception e) {
            throw new DataImportException("Failed to export reduction strategies", e);
        }
    }

    // CARBON METRICS METHODS

    // Export data from CarbonFootprintService to CSV
    public void exportCarbonMetricsToCSV(List<CarbonFootprintMetrics> metrics, OutputStream outputStream)
            throws DataImportException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("BranchID", "CityName", "DistributionEmissions",
                        "PackagingEmissions", "ProductionEmissions", "TotalEmissions")
                .build();
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, format)) {

            for (CarbonFootprintMetrics metric : metrics) {
                printer.printRecord(
                        metric.getBranchId(),
                        metric.getCityName(),
                        metric.getDistributionEmissions(),
                        metric.getPackagingEmissions(),
                        metric.getProductionEmissions(),
                        metric.getTotalEmissions()
                );
            }
        } catch (Exception e) {
            throw new DataImportException("Failed to export metrics", e);
        }
    }

    // BRANCH DATA METHODS

    // Import CSV and save to database
    public void importAndSaveBranches(InputStream inputStream, int userId)
            throws DataImportException, DataAccessException {
        List<Branch> branches = importBranchesFromCSV(inputStream);

        for (Branch branch : branches) {
            boolean success = branchDao.save(branch);
            if (success) {
                logAction(userId, "INSERT", "Branch", branch.getBranchId());
            }
        }
    }

    // Export data from database to CSV
    public void exportBranchesToCSV(OutputStream outputStream, int cityId)
            throws DataImportException, DataAccessException {
        List<Branch> branches;
        if (cityId > 0) {
            branches = branchDao.getBranchesByCityId(cityId);
        } else {
            branches = branchDao.getAll();
        }

        exportBranchesToCSV(branches, outputStream);
    }

    // Original CSV parsing method (unchanged)
    public List<Branch> importBranchesFromCSV(InputStream inputStream) throws DataImportException {
        List<Branch> branches = new ArrayList<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, format)) {

            for (CSVRecord record : parser) {
                Branch branch = new Branch();
                branch.setBranchId(Integer.parseInt(record.get("BranchID")));
                branch.setCityId(Integer.parseInt(record.get("CityID")));
                branch.setLocation(record.get("Location"));
                branch.setNumberOfEmployees(Integer.parseInt(record.get("NumberOfEmployees")));
                branches.add(branch);
            }
        } catch (Exception e) {
            throw new DataImportException("Failed to import branch data", e);
        }
        return branches;
    }

    // Original CSV export method (unchanged)
    public void exportBranchesToCSV(List<Branch> branches, OutputStream outputStream)
            throws DataImportException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("BranchID", "CityID", "Location", "NumberOfEmployees")
                .build();

        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, format)) {

            for (Branch branch : branches) {
                printer.printRecord(
                        branch.getBranchId(),
                        branch.getCityId(),
                        branch.getLocation(),
                        branch.getNumberOfEmployees()
                );
            }
        } catch (Exception e) {
            throw new DataImportException("Failed to export branch data", e);
        }
    }

    // Helper method for audit logging
    private void logAction(int userId, String action, String tableName, int recordId) throws DataAccessException {
        AuditLogging log = new AuditLogging(0, userId, action, tableName, recordId, LocalDateTime.now());
        auditLoggingDao.insert(log);
    }
}