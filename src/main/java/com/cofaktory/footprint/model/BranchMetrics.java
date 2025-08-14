package com.cofaktory.footprint.model;

public class BranchMetrics {
    private int branchId;
    private double carbonEmissionsKg;
    private double packagingWasteKg;
    private double productionQuantitiesKg;
    private int numberOfEmployees;
    private String location;

    public BranchMetrics(int branchId, double carbonEmissionsKg, double packagingWasteKg, double productionQuantitiesKg, int numberOfEmployees, String location) {
        this.branchId = branchId;
        this.carbonEmissionsKg = carbonEmissionsKg;
        this.packagingWasteKg = packagingWasteKg;
        this.productionQuantitiesKg = productionQuantitiesKg;
        this.numberOfEmployees = numberOfEmployees;
        this.location = location;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public double getCarbonEmissionsKg() {
        return carbonEmissionsKg;
    }

    public void setCarbonEmissionsKg(double carbonEmissionsKg) {
        this.carbonEmissionsKg = carbonEmissionsKg;
    }

    public double getPackagingWasteKg() {
        return packagingWasteKg;
    }

    public void setPackagingWasteKg(double packagingWasteKg) {
        this.packagingWasteKg = packagingWasteKg;
    }

    public double getProductionQuantitiesKg() {
        return productionQuantitiesKg;
    }

    public void setProductionQuantitiesKg(double productionQuantitiesKg) {
        this.productionQuantitiesKg = productionQuantitiesKg;
    }

    public int getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void setNumberOfEmployees(int numberOfEmployees) {
        this.numberOfEmployees = numberOfEmployees;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
}