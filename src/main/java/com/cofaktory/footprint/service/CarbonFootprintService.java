package com.cofaktory.footprint.service;

import com.cofaktory.footprint.dao.*;
import com.cofaktory.footprint.model.*;
import com.cofaktory.footprint.myExceptions.DataAccessException;
import java.util.List;

import static com.cofaktory.footprint.util.EmissionCalculator.*;

public class CarbonFootprintService {
    private final CoffeeDistributionDao distributionDao;
    private final CoffeePackagingDao packagingDao;
    private final CoffeeProductionDao productionDao;
    private final BranchDao branchDao;
    private final CityDao cityDao;

    public CarbonFootprintService(CoffeeDistributionDao distributionDao,
                                  CoffeePackagingDao packagingDao,
                                  CoffeeProductionDao productionDao,
                                  BranchDao branchDao,
                                  CityDao cityDao) {
        this.distributionDao = distributionDao;
        this.packagingDao = packagingDao;
        this.productionDao = productionDao;
        this.branchDao = branchDao;
        this.cityDao = cityDao;
    }

    public CarbonFootprintMetrics getCarbonFootprintMetrics(int branchId) throws DataAccessException {
        CarbonFootprintMetrics metrics = new CarbonFootprintMetrics();
        metrics.setBranchId(branchId);

        Branch branch = branchDao.getById(branchId);
        if (branch != null) {
            City city = cityDao.getById(branch.getCityId());
            metrics.setCityName(city != null ? city.getCityName() : "Unknown");
        }

        metrics.setDistributionEmissions(distributionDao.getTotalCarbonEmissionsByBranchId(branchId));
        metrics.setPackagingEmissions(packagingDao.getTotalCarbonEmissionsByBranchId(branchId));
        metrics.setProductionEmissions(productionDao.getTotalCarbonEmissionsByBranchId(branchId));
        metrics.setTotalEmissions(metrics.getDistributionEmissions() +
                metrics.getPackagingEmissions() +
                metrics.getProductionEmissions());

        return metrics;
    }

    public List<CoffeeDistribution> getHighEmissionDistributions(double threshold) throws DataAccessException {
        return distributionDao.getHighEmissionDistributions(threshold);
    }

    public List<CoffeeProduction> getProductionByType(String coffeeType) throws DataAccessException {
        return productionDao.getProductionByCoffeeType(coffeeType);
    }

    public void recalculateAllEmissions() throws DataAccessException {
        // Get all records from database
        List<CoffeeProduction> productions = productionDao.getAll();
        List<CoffeePackaging> packagings = packagingDao.getAll();
        List<CoffeeDistribution> distributions = distributionDao.getAll();

        // Recalculate and update
        for (CoffeeProduction production : productions) {
            calculateProductionEmissions(production.getProductionQuantitiesOfCoffeeKG());
            productionDao.update(production);
        }

        for (CoffeePackaging packaging : packagings) {
            calculatePackagingEmissions(packaging.getPackagingWasteKG());
            packagingDao.update(packaging);
        }

        for (CoffeeDistribution distribution : distributions) {
            calculateDistributionEmissions(distribution.getVehicleType(),distribution.getDistancePerVehicleKM(),distribution.getNumberOfVehicles());
            distributionDao.update(distribution);
        }

        // Similar for packaging and distribution
    }
}