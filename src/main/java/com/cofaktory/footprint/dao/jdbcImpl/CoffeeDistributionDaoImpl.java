package com.cofaktory.footprint.dao.jdbcImpl;

import com.cofaktory.footprint.dao.CoffeeDistributionDao;
import com.cofaktory.footprint.model.CoffeeDistribution;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoffeeDistributionDaoImpl implements CoffeeDistributionDao {
    private final DataSource dataSource;

    public CoffeeDistributionDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public CoffeeDistribution getById(int id) throws DataAccessException {
        String sql = "SELECT * FROM CoffeeDistribution WHERE DistributionID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapToCoffeeDistribution(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get coffee distribution by ID: " + id, e);
        }
        return null;
    }

    @Override
    public List<CoffeeDistribution> getAll() throws DataAccessException {
        List<CoffeeDistribution> distributions = new ArrayList<>();
        String sql = "SELECT * FROM CoffeeDistribution ORDER BY DistributionID DESC";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                distributions.add(mapToCoffeeDistribution(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get all coffee distributions", e);
        }
        return distributions;
    }

    @Override
    public boolean save(CoffeeDistribution distribution) throws DataAccessException {
        if (distribution.getDistributionId() > 0) {
            return update(distribution);
        } else {
            return insert(distribution);
        }
    }

    @Override
    public List<CoffeeDistribution> getHighEmissionDistributions(double thresholdKg) throws DataAccessException {
        List<CoffeeDistribution> distributions = new ArrayList<>();
        String sql = "SELECT * FROM CoffeeDistribution WHERE V_CarbonEmissions_Kg > ? ORDER BY V_CarbonEmissions_Kg DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, thresholdKg);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    distributions.add(mapToCoffeeDistribution(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get high emission distributions (threshold: " + thresholdKg + "kg)", e);
        }
        return distributions;
    }

    @Override
    public double getAverageDistanceByBranchId(int branchId) throws DataAccessException {
        String sql = "SELECT AVG(TotalDistance_KM) FROM CoffeeDistribution WHERE BranchID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get average distance for branch: " + branchId, e);
        }
        return 0;
    }

    @Override
    public boolean insert(CoffeeDistribution distribution) throws DataAccessException {
        // First, validate that the UserID exists in the User table
        if (!validateUserExists(distribution.getUserId())) {
            throw new DataAccessException("Invalid UserID: " + distribution.getUserId() + ". User does not exist in the database.");
        }
        
        String sql = "INSERT INTO CoffeeDistribution (BranchID, UserID, VehicleType, NumberOfVehicles, DistancePerVehicle_KM, ActivityDate) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, distribution.getBranchId());
            stmt.setInt(2, distribution.getUserId());
            
            // Handle null values properly
            if (distribution.getVehicleType() == null) {
                stmt.setNull(3, Types.VARCHAR);
            } else {
                stmt.setString(3, distribution.getVehicleType());
            }
            
            stmt.setInt(4, distribution.getNumberOfVehicles());
            stmt.setDouble(5, distribution.getDistancePerVehicleKM());
            
            if (distribution.getDistributionDate() == null) {
                stmt.setNull(6, Types.DATE);
            } else {
                stmt.setDate(6, distribution.getDistributionDate());
            }
            
            System.out.println("Executing insert with values: BranchID=" + distribution.getBranchId() + 
                              ", UserID=" + distribution.getUserId() + 
                              ", VehicleType=" + distribution.getVehicleType() + 
                              ", NumberOfVehicles=" + distribution.getNumberOfVehicles() + 
                              ", DistancePerVehicle=" + distribution.getDistancePerVehicleKM() + 
                              ", Date=" + distribution.getDistributionDate());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        distribution.setDistributionId(generatedKeys.getInt(1));
                        System.out.println("Generated Distribution ID: " + distribution.getDistributionId());
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("SQL Error in insert distribution: " + e.getMessage());
            e.printStackTrace();
            throw new DataAccessException("Failed to insert coffee distribution: " + e.getMessage(), e);
        }
    }

    @Override
    public List<CoffeeDistribution> getDistributionsByVehicleType(String vehicleType) throws DataAccessException {
        List<CoffeeDistribution> distributions = new ArrayList<>();
        String sql = "SELECT * FROM CoffeeDistribution WHERE VehicleType = ? ORDER BY V_CarbonEmissions_Kg DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicleType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    distributions.add(mapToCoffeeDistribution(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get distributions by vehicle type: " + vehicleType, e);
        }
        return distributions;
    }

    @Override
    public String getMostCommonVehicleTypeByBranchId(int branchId) throws DataAccessException {
        String sql = "SELECT VehicleType, COUNT(*) as count FROM CoffeeDistribution " +
                "WHERE BranchID = ? GROUP BY VehicleType ORDER BY count DESC LIMIT 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("VehicleType");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get most common vehicle type for branch: " + branchId, e);
        }
        return null;
    }
    @Override
    public boolean update(CoffeeDistribution distribution) throws DataAccessException {
        // Update only the fields we want to change, not including UserID
        String sql = "UPDATE CoffeeDistribution SET VehicleType = ?, NumberOfVehicles = ?, DistancePerVehicle_KM = ?, ActivityDate = ? WHERE DistributionID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, distribution.getVehicleType());
            stmt.setInt(2, distribution.getNumberOfVehicles());
            stmt.setDouble(3, distribution.getDistancePerVehicleKM());
            
            // Ensure the date is not null
            if (distribution.getDistributionDate() == null) {
                throw new DataAccessException("Distribution date cannot be null");
            }
            stmt.setDate(4, distribution.getDistributionDate());
            stmt.setInt(5, distribution.getDistributionId());

            System.out.println("Executing update with ID: " + distribution.getDistributionId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update coffee distribution: " + distribution.getDistributionId() + 
                                        ". Error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates that a user with the given ID exists in the User table
     * @param userId The user ID to validate
     * @return true if the user exists, false otherwise
     */
    private boolean validateUserExists(int userId) throws DataAccessException {
        String sql = "SELECT 1 FROM User WHERE UserID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Returns true if a row was found
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to validate user existence: " + userId, e);
        }
    }

    @Override
    public boolean delete(CoffeeDistribution distribution) throws DataAccessException {
        String sql = "DELETE FROM CoffeeDistribution WHERE DistributionID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, distribution.getDistributionId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete coffee distribution: " + distribution.getDistributionId(), e);
        }
    }

    @Override
    public List<CoffeeDistribution> getDistributionsByBranchId(int branchId) throws DataAccessException {
        List<CoffeeDistribution> distributions = new ArrayList<>();
        String sql = "SELECT * FROM CoffeeDistribution WHERE BranchID = ? ORDER BY DistributionID DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    distributions.add(mapToCoffeeDistribution(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get distributions by branch ID: " + branchId, e);
        }
        return distributions;
    }

    @Override
    public double getTotalCarbonEmissionsByBranchId(int branchId) throws DataAccessException {
        String sql = "SELECT SUM(V_CarbonEmissions_Kg) FROM CoffeeDistribution WHERE BranchID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get total carbon emissions for branch: " + branchId, e);
        }
        return 0;
    }

    private CoffeeDistribution mapToCoffeeDistribution(ResultSet rs) throws SQLException {
        return new CoffeeDistribution(
                rs.getInt("DistributionID"),
                rs.getInt("BranchID"),
                rs.getInt("UserID"),
                rs.getString("VehicleType"),
                rs.getInt("NumberOfVehicles"),
                rs.getDouble("DistancePerVehicle_KM"),
                rs.getDouble("TotalDistance_KM"),
                rs.getDouble("FuelEfficiency"),
                rs.getDouble("V_CarbonEmissions_Kg"),
                rs.getDate("ActivityDate")
        );
    }
}