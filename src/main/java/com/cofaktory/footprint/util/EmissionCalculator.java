package com.cofaktory.footprint.util;

public class EmissionCalculator {

    // Constants for emission factors (kg CO2 per unit)
    private static final double PACKAGING_EMISSION_FACTOR = 6.0; // per kg
    private static final double PRODUCTION_EMISSION_FACTOR = 6.4; // per kg
    private static final double MINIVAN_EMISSION_FACTOR = 10; // per km
    private static final double TRUCK_EMISSION_FACTOR = 15; // per km

    public static double calculatePackagingEmissions(double packagingWasteKg) {
        return packagingWasteKg * PACKAGING_EMISSION_FACTOR;
    }

    public static double calculateProductionEmissions(double productionQuantityKg) {
        return productionQuantityKg * PRODUCTION_EMISSION_FACTOR;
    }

    public static double calculateDistributionEmissions(String vehicleType, double distanceKm, int numberOfVehicles) {
        double factor = vehicleType.equalsIgnoreCase("minivan") ?
                MINIVAN_EMISSION_FACTOR : TRUCK_EMISSION_FACTOR;
        return distanceKm * 2 * factor * numberOfVehicles * 2.68; //2.68 is fuel efficiency
    }
}