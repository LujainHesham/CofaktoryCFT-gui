package com.cofaktory.footprint.dao.jdbcImpl;

import com.cofaktory.footprint.dao.ReductionStrategyDao;
import com.cofaktory.footprint.model.ReductionStrategy;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReductionStrategyDaoImpl implements ReductionStrategyDao {
    private final DataSource dataSource;

    public ReductionStrategyDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public ReductionStrategy getById(int id) throws DataAccessException {
        String sql = "SELECT * FROM ReductionStrategy WHERE ReductionID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapToReductionStrategy(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get reduction strategy by ID: " + id, e);
        }
        return null;
    }

    @Override
    public List<ReductionStrategy> getAll() throws DataAccessException {
        List<ReductionStrategy> strategies = new ArrayList<>();
        String sql = "SELECT * FROM ReductionStrategy";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                strategies.add(mapToReductionStrategy(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get all reduction strategies", e);
        }
        return strategies;
    }

    @Override
    public boolean save(ReductionStrategy strategy) throws DataAccessException {
        if (strategy.getReductionId() > 0) {
            return update(strategy);
        } else {
            return insert(strategy);
        }
    }

    @Override
    public boolean insert(ReductionStrategy strategy) throws DataAccessException {
        // Do not insert ProjectedAnnualProfits (it's a generated column)
        String sql = "INSERT INTO ReductionStrategy (BranchID, UserID, ReductionStrategy, StatusID, ImplementationCosts) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setStrategyParameters(stmt, strategy);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        strategy.setReductionId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert reduction strategy", e);
        }
    }

    @Override
    public boolean update(ReductionStrategy strategy) throws DataAccessException {
        // Do not update ProjectedAnnualProfits (it's a generated column)
        String sql = "UPDATE ReductionStrategy SET BranchID = ?, UserID = ?, ReductionStrategy = ?, StatusID = ?, ImplementationCosts = ? WHERE ReductionID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setStrategyParameters(stmt, strategy);
            stmt.setInt(6, strategy.getReductionId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update reduction strategy: " + strategy.getReductionId(), e);
        }
    }

    @Override
    public boolean delete(ReductionStrategy strategy) throws DataAccessException {
        return delete(strategy.getReductionId());
    }

    private boolean delete(int reductionId) throws DataAccessException {
        String sql = "DELETE FROM ReductionStrategy WHERE ReductionID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reductionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete reduction strategy: " + reductionId, e);
        }
    }

    @Override
    public List<ReductionStrategy> getReductionStrategiesByBranchId(int branchId) throws DataAccessException {
        return getStrategiesByCondition("BranchID = ?", branchId);
    }

    @Override
    public List<ReductionStrategy> getReductionStrategiesByUserId(int userId) throws DataAccessException {
        return getStrategiesByCondition("UserID = ?", userId);
    }

    @Override
    public List<ReductionStrategy> getActiveReductionStrategies() throws DataAccessException {
        List<ReductionStrategy> strategies = new ArrayList<>();
        String sql = "SELECT rs.* FROM ReductionStrategy rs " +
                "JOIN PlanStatus ps ON rs.StatusID = ps.StatusID " +
                "WHERE ps.StatusName = 'Active'";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                strategies.add(mapToReductionStrategy(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get active reduction strategies", e);
        }
        return strategies;
    }

    private List<ReductionStrategy> getStrategiesByCondition(String condition, int param) throws DataAccessException {
        List<ReductionStrategy> strategies = new ArrayList<>();
        String sql = "SELECT * FROM ReductionStrategy WHERE " + condition;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    strategies.add(mapToReductionStrategy(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get reduction strategies by condition: " + condition, e);
        }
        return strategies;
    }

    // Only set parameters for non-generated columns
    private void setStrategyParameters(PreparedStatement stmt, ReductionStrategy strategy) throws SQLException {
        stmt.setInt(1, strategy.getBranchId());
        stmt.setInt(2, strategy.getUserId());
        stmt.setString(3, strategy.getReductionStrategy());
        stmt.setInt(4, strategy.getStatusId());
        stmt.setDouble(5, strategy.getImplementationCosts());
    }

    // Map all columns, including generated column ProjectedAnnualProfits
    private ReductionStrategy mapToReductionStrategy(ResultSet rs) throws SQLException {
        return new ReductionStrategy(
                rs.getInt("ReductionID"),
                rs.getInt("BranchID"),
                rs.getInt("UserID"),
                rs.getString("ReductionStrategy"),
                rs.getInt("StatusID"),
                rs.getDouble("ImplementationCosts"),
                rs.getDouble("ProjectedAnnualProfits")
        );
    }
}