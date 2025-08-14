package com.cofaktory.footprint.model;
import java.sql.Date;

public class CoffeePackaging {
    private int packagingId;
    private int branchId;
    private int userId;
    private double packagingWasteKG;
    private double paCarbonEmissionsKG;
    private Date packagingDate;

    public CoffeePackaging(int packagingId, int branchId, int userId,
                           double packagingWasteKG, double paCarbonEmissionsKG,
                           Date packagingDate) {
        this.packagingId = packagingId;
        this.branchId = branchId;
        this.userId = userId;
        this.packagingWasteKG = packagingWasteKG;
        this.paCarbonEmissionsKG = paCarbonEmissionsKG;
        this.packagingDate = packagingDate;
    }

    public CoffeePackaging() {}

    public int getPackagingId() { return packagingId; }
    public void setPackagingId(int packagingId) { this.packagingId = packagingId; }
    public int getBranchId() { return branchId; }
    public void setBranchId(int branchId) { this.branchId = branchId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public double getPackagingWasteKG() { return packagingWasteKG; }
    public void setPackagingWasteKG(double packagingWasteKG) { this.packagingWasteKG = packagingWasteKG; }
    public double getPaCarbonEmissionsKG() { return paCarbonEmissionsKG; }
    public void setPaCarbonEmissionsKG(double paCarbonEmissionsKG) { this.paCarbonEmissionsKG = paCarbonEmissionsKG; }
    public Date getPackagingDate() { return packagingDate; }
    public void setPackagingDate(Date packagingDate) { this.packagingDate = packagingDate; }
    
    @Override
    public String toString() {
        return "CoffeePackaging{" +
                "packagingId=" + packagingId +
                ", branchId=" + branchId +
                ", userId=" + userId +
                ", packagingWasteKG=" + packagingWasteKG +
                ", paCarbonEmissionsKG=" + paCarbonEmissionsKG +
                ", packagingDate=" + packagingDate +
                '}';
    }
}