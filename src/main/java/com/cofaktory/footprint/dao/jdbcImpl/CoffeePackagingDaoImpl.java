package com.cofaktory.footprint.dao.jdbcImpl;

import com.cofaktory.footprint.dao.CoffeePackagingDao;
import com.cofaktory.footprint.model.CoffeePackaging;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoffeePackagingDaoImpl implements CoffeePackagingDao {
    private final DataSource dataSource;

    public CoffeePackagingDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public CoffeePackaging getById(int id) throws DataAccessException {
        String sql = "SELECT * FROM CoffeePackaging WHERE PackagingID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapToCoffeePackaging(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get coffee packaging by ID: " + id, e);
        }
        return null;
    }

    @Override
    public List<CoffeePackaging> getAll() throws DataAccessException {
        List<CoffeePackaging> packagingList = new ArrayList<>();
        String sql = "SELECT * FROM CoffeePackaging ORDER BY PackagingID DESC";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                packagingList.add(mapToCoffeePackaging(rs));
            }
            
            System.out.println("Retrieved " + packagingList.size() + " packaging records from database");
            if (packagingList.isEmpty()) {
                System.out.println("No packaging records found in database");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getAll: " + e.getMessage());
            e.printStackTrace();
            throw new DataAccessException("Failed to get all coffee packaging: " + e.getMessage(), e);
        }
        return packagingList;
    }

    @Override
    public boolean save(CoffeePackaging packaging) throws DataAccessException {
        if (packaging.getPackagingId() > 0) {
            return update(packaging);
        } else {
            return insert(packaging);
        }
    }

    @Override
    public boolean insert(CoffeePackaging packaging) throws DataAccessException {
        // First check if the user ID exists
        if (!validateUserExists(packaging.getUserId())) {
            throw new DataAccessException("Invalid UserID: " + packaging.getUserId() + ". User does not exist in the database.");
        }
        
        String sql = "INSERT INTO CoffeePackaging (BranchID, UserID, PackagingWaste_KG, ActivityDate) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, packaging.getBranchId());
            stmt.setInt(2, packaging.getUserId());
            stmt.setDouble(3, packaging.getPackagingWasteKG());
            
            // Handle null date
            if (packaging.getPackagingDate() == null) {
                stmt.setNull(4, Types.DATE);
            } else {
                stmt.setDate(4, packaging.getPackagingDate());
            }
            
            System.out.println("Executing insert for packaging with values: BranchID=" + packaging.getBranchId() + 
                              ", UserID=" + packaging.getUserId() + 
                              ", PackagingWaste=" + packaging.getPackagingWasteKG() + 
                              ", Date=" + packaging.getPackagingDate());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        packaging.setPackagingId(generatedKeys.getInt(1));
                        System.out.println("Generated Packaging ID: " + packaging.getPackagingId());
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("SQL Error in insert packaging: " + e.getMessage());
            e.printStackTrace();
            throw new DataAccessException("Failed to insert coffee packaging: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(CoffeePackaging packaging) throws DataAccessException {
        // Get the original record to preserve the UserID
        CoffeePackaging original = getById(packaging.getPackagingId());
        if (original == null) {
            throw new DataAccessException("Cannot update packaging with ID " + packaging.getPackagingId() + ": record not found");
        }
        
        // Use the original UserID to avoid foreign key issues
        int userId = original.getUserId();
        
        // Only update the fields we want to change, not including UserID
        String sql = "UPDATE CoffeePackaging SET BranchID = ?, PackagingWaste_KG = ?, ActivityDate = ? WHERE PackagingID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, packaging.getBranchId());
            stmt.setDouble(2, packaging.getPackagingWasteKG());
            stmt.setDate(3, packaging.getPackagingDate());
            stmt.setInt(4, packaging.getPackagingId());

            System.out.println("Executing update for packaging ID: " + packaging.getPackagingId());
            System.out.println("Keeping original UserID: " + userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update coffee packaging: " + packaging.getPackagingId() + ". Error: " + e.getMessage(), e);
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
    public boolean delete(CoffeePackaging packaging) throws DataAccessException {
        return delete(packaging.getPackagingId());
    }

    private boolean delete(int packagingId) throws DataAccessException {
        String sql = "DELETE FROM CoffeePackaging WHERE PackagingID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, packagingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete coffee packaging: " + packagingId, e);
        }
    }

    @Override
    public List<CoffeePackaging> getPackagingWasteByBranchId(int branchId) throws DataAccessException {
        List<CoffeePackaging> packagingList = new ArrayList<>();
        String sql = "SELECT * FROM CoffeePackaging WHERE BranchID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    packagingList.add(mapToCoffeePackaging(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get packaging waste by branch ID: " + branchId, e);
        }
        return packagingList;
    }

    @Override
    public double getTotalCarbonEmissionsByBranchId(int branchId) throws DataAccessException {
        String sql = "SELECT SUM(Pa_CarbonEmissions_KG) FROM CoffeePackaging WHERE BranchID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get total carbon emissions by branch ID: " + branchId, e);
        }
        return 0;
    }

    private CoffeePackaging mapToCoffeePackaging(ResultSet rs) throws SQLException {
        try {
            int packagingId = rs.getInt("PackagingID");
            int branchId = rs.getInt("BranchID");
            int userId = rs.getInt("UserID");
            double packagingWaste = rs.getDouble("PackagingWaste_KG");
            double carbonEmissions = rs.getDouble("Pa_CarbonEmissions_KG");
            Date activityDate = rs.getDate("ActivityDate");
            
            System.out.println("Mapping record from database:");
            System.out.println("- PackagingID: " + packagingId);
            System.out.println("- BranchID: " + branchId);
            System.out.println("- UserID: " + userId);
            System.out.println("- PackagingWaste_KG: " + packagingWaste);
            System.out.println("- Pa_CarbonEmissions_KG: " + carbonEmissions);
            System.out.println("- ActivityDate: " + activityDate);
            
            CoffeePackaging packaging = new CoffeePackaging(
                packagingId,
                branchId,
                userId,
                packagingWaste,
                carbonEmissions,
                activityDate
            );
            
            return packaging;
        } catch (SQLException e) {
            System.err.println("Error mapping packaging record: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}