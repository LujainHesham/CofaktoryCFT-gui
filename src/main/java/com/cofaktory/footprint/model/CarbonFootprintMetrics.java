package com.cofaktory.footprint.model;

public class CarbonFootprintMetrics {

    private int branchId;
    private String cityName;
    private double distributionEmissions;
    private double packagingEmissions;
    private double productionEmissions;
    private double totalEmissions;

    // Getters and Setters
    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public double getDistributionEmissions() {
        return distributionEmissions;
    }

    public void setDistributionEmissions(double distributionEmissions) {
        this.distributionEmissions = distributionEmissions;
    }

    public double getPackagingEmissions() {
        return packagingEmissions;
    }

    public void setPackagingEmissions(double packagingEmissions) {
        this.packagingEmissions = packagingEmissions;
    }

    public double getProductionEmissions() {
        return productionEmissions;
    }

    public void setProductionEmissions(double productionEmissions) {
        this.productionEmissions = productionEmissions;
    }

    public double getTotalEmissions() {
        return totalEmissions;
    }

    public void setTotalEmissions(double totalEmissions) {
        this.totalEmissions = totalEmissions;
    }
}