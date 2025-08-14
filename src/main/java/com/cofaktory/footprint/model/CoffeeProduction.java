package com.cofaktory.footprint.model;

import java.sql.Date;

public class CoffeeProduction {
    private int productionId;
    private int branchId;
    private int userId;
    private String supplier;
    private String coffeeType;
    private String productType;
    private double productionQuantitiesOfCoffeeKG;
    private double prCarbonEmissionsKG;
    private Date productionDate;

    public CoffeeProduction() {}

    public CoffeeProduction(int productionId, int branchId, int userId, String supplier,
                            String coffeeType, String productType,
                            double productionQuantitiesOfCoffeeKG, double prCarbonEmissionsKG,
                            Date productionDate) {
        this.productionId = productionId;
        this.branchId = branchId;
        this.userId = userId;
        this.supplier = supplier;
        this.coffeeType = coffeeType;
        this.productType = productType;
        this.productionQuantitiesOfCoffeeKG = productionQuantitiesOfCoffeeKG;
        this.prCarbonEmissionsKG = prCarbonEmissionsKG;
        this.productionDate = productionDate;
    }

    public int getProductionId() { return productionId; }
    public void setProductionId(int productionId) { this.productionId = productionId; }

    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getCoffeeType() { return coffeeType; }
    public void setCoffeeType(String coffeeType) { this.coffeeType = coffeeType; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public double getProductionQuantitiesOfCoffeeKG() { return productionQuantitiesOfCoffeeKG; }
    public void setProductionQuantitiesOfCoffeeKG(double v) { this.productionQuantitiesOfCoffeeKG = v; }

    public double getPrCarbonEmissionsKG() { return prCarbonEmissionsKG; }
    public void setPrCarbonEmissionsKG(double v) { this.prCarbonEmissionsKG = v; }

    public Date getProductionDate() { return productionDate; }
    public void setProductionDate(Date productionDate) { this.productionDate = productionDate; }
}