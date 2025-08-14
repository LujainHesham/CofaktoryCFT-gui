package com.cofaktory.footprint.model;

public class Branch {
    private int branchId;
    private int cityId;
    private String location;
    private int numberOfEmployees;

    public Branch(int branchId, int cityId, String location, int numberOfEmployees) {
        this.branchId = branchId;
        this.cityId = cityId;
        this.location = location;
        this.numberOfEmployees = numberOfEmployees;
    }

    public Branch() {

    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void setNumberOfEmployees(int numberOfEmployees) {
        this.numberOfEmployees = numberOfEmployees;
    }

    @Override
    public String toString() {
        return String.format("Branch #%d - %s", branchId, location);
    }
}

