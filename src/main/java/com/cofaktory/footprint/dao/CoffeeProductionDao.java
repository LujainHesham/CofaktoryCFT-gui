package com.cofaktory.footprint.dao;

import com.cofaktory.footprint.model.CoffeeProduction;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import java.util.List;

public interface CoffeeProductionDao extends DAO<CoffeeProduction> {
    // Get production quantities by branch ID
    List<CoffeeProduction> getProductionQuantitiesByBranchId(int branchId) throws DataAccessException;

    // Get total carbon emissions from production for a branch
    double getTotalCarbonEmissionsByBranchId(int branchId) throws DataAccessException;

    // Get production data by coffee type
    List<CoffeeProduction> getProductionByCoffeeType(String coffeeType) throws DataAccessException;
    
    CoffeeProduction getByBranchId(int branchId) throws DataAccessException;


    
}