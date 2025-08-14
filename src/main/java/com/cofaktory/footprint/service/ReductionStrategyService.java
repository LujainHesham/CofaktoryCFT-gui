package com.cofaktory.footprint.service;

import com.cofaktory.footprint.dao.ReductionStrategyDao;
import com.cofaktory.footprint.model.ReductionStrategy;
import com.cofaktory.footprint.myExceptions.DataAccessException;
import java.util.List;

public class ReductionStrategyService {
    private final ReductionStrategyDao strategyDao;

    public ReductionStrategyService(ReductionStrategyDao strategyDao) {
        this.strategyDao = strategyDao;
    }

    public boolean saveStrategy(ReductionStrategy strategy) throws DataAccessException {
        return strategyDao.save(strategy);
    }

    public List<ReductionStrategy> getStrategiesByBranch(int branchId) throws DataAccessException {
        return strategyDao.getReductionStrategiesByBranchId(branchId);
    }

    public List<ReductionStrategy> getActiveStrategies() throws DataAccessException {
        return strategyDao.getActiveReductionStrategies();
    }

    public boolean deleteStrategy(ReductionStrategy strategy) throws DataAccessException {
        return strategyDao.delete(strategy);
    }

    public List<ReductionStrategy> getAllStrategies() throws DataAccessException {
        return strategyDao.getAll();
    }

    // For single reduction plan PDF generation (used in controller)
    public ReductionStrategy getById(int reductionId) throws DataAccessException {
        return strategyDao.getById(reductionId);
    }
}