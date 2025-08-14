package com.cofaktory.footprint.dao;

import com.cofaktory.footprint.model.CoffeeDistribution;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import java.util.List;

public interface CoffeeDistributionDao extends DAO<CoffeeDistribution> {


    List<CoffeeDistribution> getDistributionsByBranchId(int branchId) throws DataAccessException;

    List<CoffeeDistribution> getDistributionsByVehicleType(String vehicleType) throws DataAccessException;

    double getTotalCarbonEmissionsByBranchId(int branchId) throws DataAccessException;


    List<CoffeeDistribution> getHighEmissionDistributions(double thresholdKg) throws DataAccessException;

    double getAverageDistanceByBranchId(int branchId) throws DataAccessException;


    String getMostCommonVehicleTypeByBranchId(int branchId) throws DataAccessException;

}