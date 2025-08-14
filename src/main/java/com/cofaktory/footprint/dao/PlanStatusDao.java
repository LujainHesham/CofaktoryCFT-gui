package com.cofaktory.footprint.dao;

import com.cofaktory.footprint.model.PlanStatus;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import java.util.List;

public interface PlanStatusDao extends DAO<PlanStatus> {
    List<PlanStatus> getPlanStatusesByBranchId(int branchId) throws DataAccessException;
}