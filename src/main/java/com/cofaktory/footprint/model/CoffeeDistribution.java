package com.cofaktory.footprint.model;
import java.sql.Date;

public class CoffeeDistribution {
    private int distributionId;
    private int branchId;
    private int userId;
    private String vehicleType;
    private int numberOfVehicles;
    private double distancePerVehicleKM;
    private double totalDistanceKM;
    private double fuelEfficiency;
    private double vCarbonEmissionsKg;
    // Maps to ActivityDate column in the database
    private Date distributionDate;

    public CoffeeDistribution(int distributionId, int branchId, int userId, String vehicleType,
                              int numberOfVehicles, double distancePerVehicleKM,
                              double totalDistanceKM, double fuelEfficiency, double vCarbonEmissionsKg,
                              Date distributionDate) {
        this.distributionId = distributionId;
        this.branchId = branchId;
        this.userId = userId;
        this.vehicleType = vehicleType;
        this.numberOfVehicles = numberOfVehicles;
        this.distancePerVehicleKM = distancePerVehicleKM;
        this.totalDistanceKM = totalDistanceKM;
        this.fuelEfficiency = fuelEfficiency;
        this.vCarbonEmissionsKg = vCarbonEmissionsKg;
        this.distributionDate = distributionDate;
    }

    public CoffeeDistribution() {}

    public int getDistributionId() { return distributionId; }
    public void setDistributionId(int distributionId) { this.distributionId = distributionId; }
    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public int getNumberOfVehicles() { return numberOfVehicles; }
    public void setNumberOfVehicles(int numberOfVehicles) { this.numberOfVehicles = numberOfVehicles; }
    public double getDistancePerVehicleKM() { return distancePerVehicleKM; }
    public void setDistancePerVehicleKM(double distancePerVehicleKM) { this.distancePerVehicleKM = distancePerVehicleKM; }
    public double getTotalDistanceKM() { return totalDistanceKM; }
    public void setTotalDistanceKM(double totalDistanceKM) { this.totalDistanceKM = totalDistanceKM; }
    public double getFuelEfficiency() { return fuelEfficiency; }
    public void setFuelEfficiency(double fuelEfficiency) { this.fuelEfficiency = fuelEfficiency; }
    public double getVCarbonEmissionsKg() { return vCarbonEmissionsKg; }
    public void setVCarbonEmissionsKg(double vCarbonEmissionsKg) { this.vCarbonEmissionsKg = vCarbonEmissionsKg; }
    public Date getDistributionDate() { return distributionDate; }
    public void setDistributionDate(Date distributionDate) { this.distributionDate = distributionDate; }
}