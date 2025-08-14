package com.cofaktory.footprint.dao.jdbcImpl;

import com.cofaktory.footprint.dao.BranchDao;
import com.cofaktory.footprint.model.Branch;
import com.cofaktory.footprint.model.BranchMetrics;
import com.cofaktory.footprint.model.User;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BranchDaoImpl implements BranchDao {
    private final DataSource dataSource;

    public BranchDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Branch mapBranchFromResultSet(ResultSet rs) throws SQLException {
        return new Branch(
                rs.getInt("BranchID"),
                rs.getInt("CityID"),
                rs.getString("Location"),
                rs.getInt("NumberOfEmployees")
        );
    }

    @Override
    public Branch getById(int id) throws DataAccessException {
        String sql = "SELECT * FROM Branch WHERE BranchID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapBranchFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get branch by ID: " + id, e);
        }
        return null;
    }


    @Override
    public List<Branch> getAll() throws DataAccessException {
        List<Branch> branches = new ArrayList<>();
        String sql = "SELECT * FROM Branch";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                branches.add(mapBranchFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get all branches", e);
        }
        return branches;
    }

    @Override
    public boolean save(Branch branch) throws DataAccessException {
        if (branch.getBranchId() > 0) {
            return update(branch);
        } else {
            return insert(branch);
        }
    }

    @Override
    public boolean insert(Branch branch) throws DataAccessException {
        String sql = "INSERT INTO Branch (CityID, Location, NumberOfEmployees) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, branch.getCityId());
            stmt.setString(2, branch.getLocation());
            stmt.setInt(3, branch.getNumberOfEmployees());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        branch.setBranchId(generatedId);
                        System.out.println("Generated Branch ID: " + generatedId);
                        return true;
                    } else {
                        System.err.println("Failed to get generated ID for new branch");
                        return false;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("SQL Error in insert branch: " + e.getMessage());
            e.printStackTrace();
            throw new DataAccessException("Failed to insert branch: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Branch branch) throws DataAccessException {
        String sql = "UPDATE Branch SET CityID = ?, Location = ?, NumberOfEmployees = ? WHERE BranchID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branch.getCityId());
            stmt.setString(2, branch.getLocation());
            stmt.setInt(3, branch.getNumberOfEmployees());
            stmt.setInt(4, branch.getBranchId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update branch: " + branch.getBranchId(), e);
        }
    }



    @Override
    public boolean delete(Branch branch) throws DataAccessException {
        String sql = "DELETE FROM Branch WHERE BranchID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branch.getBranchId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete branch: " + branch.getBranchId(), e);
        }
    }

    @Override
    public boolean branchExists(int branchId) throws DataAccessException {
        String sql = "SELECT 1 FROM Branch WHERE BranchID = ? LIMIT 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check if branch exists: " + branchId, e);
        }
    }

    @Override
    public List<Branch> getBranchesByCityId(int cityId) throws DataAccessException {
        List<Branch> branches = new ArrayList<>();
        String sql = "SELECT * FROM Branch WHERE CityID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cityId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    branches.add(mapBranchFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get branches by city ID: " + cityId, e);
        }
        return branches;
    }

    @Override
    public int countBranchesByCityId(int cityId) throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM Branch WHERE CityID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cityId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count branches by city ID: " + cityId, e);
        }
        return 0;
    }

    @Override
    public BranchMetrics getBranchMetrics(int branchId) throws DataAccessException {
        // Implementation depends on your BranchMetrics structure
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public User getBranchUser(int branchId) throws DataAccessException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Branch> getBranchesWithMostEmployees(int limit) throws DataAccessException {
        List<Branch> branches = new ArrayList<>();
        String sql = "SELECT * FROM Branch ORDER BY NumberOfEmployees DESC LIMIT ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    branches.add(mapBranchFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get branches with most employees", e);
        }
        return branches;
    }


}