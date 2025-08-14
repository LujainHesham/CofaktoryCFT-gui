package com.cofaktory.footprint.dao;

import com.cofaktory.footprint.model.CoffeePackaging;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import java.util.List;

public interface CoffeePackagingDao extends DAO<CoffeePackaging> {
    // Get packaging waste by branch ID
    List<CoffeePackaging> getPackagingWasteByBranchId(int branchId) throws DataAccessException;

    // Get total carbon emissions from packaging for a branch
    double getTotalCarbonEmissionsByBranchId(int branchId) throws DataAccessException;
}