package com.cofaktory.footprint.dao;
import com.cofaktory.footprint.model.Branch;
import com.cofaktory.footprint.model.BranchMetrics;
import com.cofaktory.footprint.model.User;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import java.util.List;
public interface BranchDao extends DAO<Branch>{


    // Custom methods
    List<Branch> getBranchesByCityId(int cityId)throws DataAccessException;
    int countBranchesByCityId(int cityId)throws DataAccessException;
    BranchMetrics getBranchMetrics(int branchId)throws DataAccessException; // Assuming BranchMetrics is a defined model
    User getBranchUser(int branchId)throws DataAccessException;
    boolean branchExists(int branchId)throws DataAccessException;
    List<Branch> getBranchesWithMostEmployees(int limit)throws DataAccessException;
}
