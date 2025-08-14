package com.cofaktory.footprint.service;

import com.cofaktory.footprint.model.CoffeeDistribution;
import com.cofaktory.footprint.model.CoffeePackaging;
import com.cofaktory.footprint.model.CoffeeProduction;
import com.cofaktory.footprint.util.EmissionCalculator;

public class EmissionService {
    public void calculateProductionEmissions(CoffeeProduction production) {
        production.setPrCarbonEmissionsKG(
                EmissionCalculator.calculateProductionEmissions(production.getProductionQuantitiesOfCoffeeKG())
        );
    }

    public void calculatePackagingEmissions(CoffeePackaging packaging) {
        packaging.setPaCarbonEmissionsKG(
                EmissionCalculator.calculatePackagingEmissions(packaging.getPackagingWasteKG())
        );
    }

    public void calculateDistributionEmissions(CoffeeDistribution distribution) {

        distribution.setVCarbonEmissionsKg(
                EmissionCalculator.calculateDistributionEmissions(
                        distribution.getVehicleType(),
                        distribution.getDistancePerVehicleKM(),
                        distribution.getNumberOfVehicles()
                )
        );
    }
}