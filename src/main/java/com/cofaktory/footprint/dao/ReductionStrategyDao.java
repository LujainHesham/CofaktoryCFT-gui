package com.cofaktory.footprint.dao;

import com.cofaktory.footprint.model.ReductionStrategy;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import java.util.List;

public interface ReductionStrategyDao extends DAO<ReductionStrategy> {
    // Get reduction strategies by branch ID
    List<ReductionStrategy> getReductionStrategiesByBranchId(int branchId) throws DataAccessException;

    // Get reduction strategies by user ID
    List<ReductionStrategy> getReductionStrategiesByUserId(int userId) throws DataAccessException;

    // Get active reduction strategies
    List<ReductionStrategy> getActiveReductionStrategies() throws DataAccessException;
}