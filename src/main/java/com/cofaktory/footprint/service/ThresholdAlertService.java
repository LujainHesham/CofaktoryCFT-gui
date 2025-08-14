package com.cofaktory.footprint.service;

import com.cofaktory.footprint.dao.*;
import com.cofaktory.footprint.model.*;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import java.time.LocalDateTime;
import java.util.*;

public class ThresholdAlertService {
    private final CoffeeProductionDao productionDao;
    private final CoffeePackagingDao packagingDao;
    private final CoffeeDistributionDao distributionDao;
    private final NotificationService notificationService;
    private final Map<String, Double> thresholds;

    public ThresholdAlertService(CoffeeProductionDao productionDao,
                                 CoffeePackagingDao packagingDao,
                                 CoffeeDistributionDao distributionDao,
                                 NotificationService notificationService) {
        this.productionDao = productionDao;
        this.packagingDao = packagingDao;
        this.distributionDao = distributionDao;
        this.notificationService = notificationService;
        this.thresholds = loadDefaultThresholds();
    }

    public void checkBranchThresholds(int branchId, int userId) throws DataAccessException {
        double productionEmissions = productionDao.getTotalCarbonEmissionsByBranchId(branchId);
        double packagingEmissions = packagingDao.getTotalCarbonEmissionsByBranchId(branchId);
        double distributionEmissions = distributionDao.getTotalCarbonEmissionsByBranchId(branchId);

        checkAndNotify("production", productionEmissions, branchId, userId);
        checkAndNotify("packaging", packagingEmissions, branchId, userId);
        checkAndNotify("distribution", distributionEmissions, branchId, userId);
    }

    private void checkAndNotify(String type, double value, int branchId, int userId)
            throws DataAccessException {
        if (value > thresholds.get(type)) {
            String message = String.format(
                    "Threshold exceeded for %s emissions in branch %d: %.2f kg CO2",
                    type, branchId, value);

            Notification alert = new Notification(
                    0, userId, message, LocalDateTime.now(), false);
            notificationService.sendNotification(alert);
        }
    }

    private Map<String, Double> loadDefaultThresholds() {
        Map<String, Double> defaults = new HashMap<>();
        defaults.put("production", 1000.0); // kg CO2
        defaults.put("packaging", 500.0);
        defaults.put("distribution", 1500.0);
        return defaults;
    }

    public void setThreshold(String type, double value) {
        thresholds.put(type, value);
    }
}