package com.cofaktory.footprint.dao.jdbcImpl;

import com.cofaktory.footprint.dao.PlanStatusDao;
import com.cofaktory.footprint.model.PlanStatus;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanStatusDaoImpl implements PlanStatusDao {
    private final DataSource dataSource;

    public PlanStatusDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public PlanStatus getById(int id) throws DataAccessException {
        String sql = "SELECT * FROM PlanStatus WHERE StatusID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PlanStatus(
                            rs.getInt("StatusID"),
                            rs.getString("StatusName")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get plan status by ID: " + id, e);
        }
        return null;
    }

    @Override
    public List<PlanStatus> getAll() throws DataAccessException {
        List<PlanStatus> statuses = new ArrayList<>();
        String sql = "SELECT * FROM PlanStatus";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                statuses.add(new PlanStatus(
                        rs.getInt("StatusID"),
                        rs.getString("StatusName")
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get all plan statuses", e);
        }
        return statuses;
    }

    @Override
    public boolean save(PlanStatus status) throws DataAccessException {
        if (status.getStatusId() > 0) {
            return update(status);
        } else {
            return insert(status);
        }
    }

    @Override
    public boolean insert(PlanStatus status) throws DataAccessException {
        String sql = "INSERT INTO PlanStatus (StatusName) VALUES (?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, status.getStatusName());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        status.setStatusId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert plan status", e);
        }
    }

    @Override
    public boolean update(PlanStatus status) throws DataAccessException {
        String sql = "UPDATE PlanStatus SET StatusName = ? WHERE StatusID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.getStatusName());
            stmt.setInt(2, status.getStatusId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update plan status: " + status.getStatusId(), e);
        }
    }

    @Override
    public boolean delete(PlanStatus status) throws DataAccessException {
        return delete(status.getStatusId());
    }

    private boolean delete(int statusId) throws DataAccessException {
        String sql = "DELETE FROM PlanStatus WHERE StatusID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, statusId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete plan status: " + statusId, e);
        }
    }

    @Override
    public List<PlanStatus> getPlanStatusesByBranchId(int branchId) throws DataAccessException {
        List<PlanStatus> statuses = new ArrayList<>();
        String sql = "SELECT ps.* FROM PlanStatus ps " +
                "JOIN ReductionStrategy rs ON ps.StatusID = rs.StatusID " +
                "WHERE rs.BranchID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    statuses.add(new PlanStatus(
                            rs.getInt("StatusID"),
                            rs.getString("StatusName")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get plan statuses by branch ID: " + branchId, e);
        }
        return statuses;
    }
}