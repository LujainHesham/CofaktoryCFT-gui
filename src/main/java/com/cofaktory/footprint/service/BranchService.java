package com.cofaktory.footprint.service;

import com.cofaktory.footprint.dao.BranchDao;
import com.cofaktory.footprint.model.Branch;
import com.cofaktory.footprint.model.BranchMetrics;
import com.cofaktory.footprint.myExceptions.DataAccessException;
import java.util.List;

public class BranchService {
    private final BranchDao branchDao;

    public BranchService(BranchDao branchDao) {
        this.branchDao = branchDao;
    }

    public Branch getBranchById(int id) throws DataAccessException {
        return branchDao.getById(id);
    }

    public List<Branch> getAllBranches() throws DataAccessException {
        return branchDao.getAll();
    }

    public boolean saveBranch(Branch branch) throws DataAccessException {
        return branchDao.save(branch);
    }

    public boolean deleteBranch(Branch branch) throws DataAccessException {
        return branchDao.delete(branch);
    }

    public List<Branch> getBranchesByCity(int cityId) throws DataAccessException {
        return branchDao.getBranchesByCityId(cityId);
    }

    public BranchMetrics getBranchMetrics(int branchId) throws DataAccessException {
        return branchDao.getBranchMetrics(branchId);
    }

    public List<Branch> getTopBranchesByEmployees(int limit) throws DataAccessException {
        return branchDao.getBranchesWithMostEmployees(limit);
    }
}