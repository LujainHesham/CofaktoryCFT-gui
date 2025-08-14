package com.cofaktory.footprint.dao.jdbcImpl;

import com.cofaktory.footprint.dao.CoffeeProductionDao;
import com.cofaktory.footprint.model.CoffeeProduction;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoffeeProductionDaoImpl implements CoffeeProductionDao {
    private final DataSource dataSource;

    public CoffeeProductionDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public CoffeeProduction getById(int id) throws DataAccessException {
        String sql = "SELECT * FROM CoffeeProduction WHERE ProductionID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapToCoffeeProduction(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get coffee production by ID: " + id, e);
        }
        return null;
    }

    @Override
    public List<CoffeeProduction> getAll() throws DataAccessException {
        List<CoffeeProduction> productions = new ArrayList<>();
        String sql = "SELECT * FROM CoffeeProduction";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Fetching all production records..."); // Debug
            while (rs.next()) {
                productions.add(mapToCoffeeProduction(rs));
            }
            System.out.println("Found " + productions.size() + " records."); // Debug
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get all coffee productions", e);
        }
        return productions;
    }

    @Override
    public boolean save(CoffeeProduction production) throws DataAccessException {
        if (production.getProductionId() > 0) {
            return update(production);
        } else {
            return insert(production);
        }
    }

    @Override
    public boolean insert(CoffeeProduction production) throws DataAccessException {
        // Validate the production object
        if (production == null) {
            throw new DataAccessException("Cannot insert null production record");
        }
        
        // Validate key fields
        if (production.getBranchId() <= 0) {
            throw new DataAccessException("Invalid branch ID: " + production.getBranchId());
        }
        
        if (production.getUserId() <= 0) {
            throw new DataAccessException("Invalid user ID: " + production.getUserId());
        }
        
        // Check if user exists
        if (!validateUserExists(production.getUserId())) {
            throw new DataAccessException("User ID " + production.getUserId() + " does not exist in the database");
        }
        
        String sql = "INSERT INTO CoffeeProduction (BranchID, UserID, Supplier, CoffeeType, ProductType, ProductionQuantitiesOfCoffee_KG, ActivityDate) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, production.getBranchId());
            stmt.setInt(2, production.getUserId());
            
            // Handle null values properly
            if (production.getSupplier() == null) {
                stmt.setNull(3, Types.VARCHAR);
            } else {
                stmt.setString(3, production.getSupplier());
            }
            
            if (production.getCoffeeType() == null) {
                stmt.setNull(4, Types.VARCHAR);
            } else {
                stmt.setString(4, production.getCoffeeType());
            }
            
            if (production.getProductType() == null) {
                stmt.setNull(5, Types.VARCHAR);
            } else {
                stmt.setString(5, production.getProductType());
            }
            
            stmt.setDouble(6, production.getProductionQuantitiesOfCoffeeKG());
            
            if (production.getProductionDate() == null) {
                stmt.setNull(7, Types.DATE);
            } else {
                stmt.setDate(7, production.getProductionDate());
            }
            
            System.out.println("Executing SQL: " + sql); // Debug
            System.out.println("With values: " + production.getBranchId() + ", " + production.getUserId() + ", " + 
                          production.getSupplier() + ", " + production.getCoffeeType() + ", " + 
                          production.getProductType() + ", " + production.getProductionQuantitiesOfCoffeeKG() + ", " + 
                          production.getProductionDate());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        production.setProductionId(generatedId);
                        System.out.println("Generated Production ID: " + generatedId);
                        return true;
                    } else {
                        System.err.println("Failed to get generated ID for production record");
                        return false;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("SQL Error in insert production: " + e.getMessage());
            e.printStackTrace();
            throw new DataAccessException("Failed to insert coffee production: " + e.getMessage(), e);
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
    public boolean update(CoffeeProduction production) throws DataAccessException {
        String sql = "UPDATE CoffeeProduction SET BranchID = ?, UserID = ?, Supplier = ?, CoffeeType = ?, ProductType = ?, ProductionQuantitiesOfCoffee_KG = ?, ActivityDate = ? WHERE ProductionID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, production.getBranchId());
            stmt.setInt(2, production.getUserId());
            stmt.setString(3, production.getSupplier());
            stmt.setString(4, production.getCoffeeType());
            stmt.setString(5, production.getProductType());
            stmt.setDouble(6, production.getProductionQuantitiesOfCoffeeKG());
            stmt.setDate(7, production.getProductionDate());
            stmt.setInt(8, production.getProductionId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update coffee production: " + production.getProductionId(), e);
        }
    }

    @Override
    public boolean delete(CoffeeProduction production) throws DataAccessException {
        return delete(production.getProductionId());
    }

    private boolean delete(int productionId) throws DataAccessException {
        String sql = "DELETE FROM CoffeeProduction WHERE ProductionID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete coffee production: " + productionId, e);
        }
    }

    @Override
    public List<CoffeeProduction> getProductionQuantitiesByBranchId(int branchId) throws DataAccessException {
        List<CoffeeProduction> productions = new ArrayList<>();
        String sql = "SELECT * FROM CoffeeProduction WHERE BranchID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    productions.add(mapToCoffeeProduction(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get production quantities by branch ID: " + branchId, e);
        }
        return productions;
    }

    @Override
    public double getTotalCarbonEmissionsByBranchId(int branchId) throws DataAccessException {
        String sql = "SELECT SUM(Pr_CarbonEmissions_KG) FROM CoffeeProduction WHERE BranchID = ?";
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

    @Override
    public List<CoffeeProduction> getProductionByCoffeeType(String coffeeType) throws DataAccessException {
        List<CoffeeProduction> productions = new ArrayList<>();
        String sql = "SELECT * FROM CoffeeProduction WHERE CoffeeType = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, coffeeType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    productions.add(mapToCoffeeProduction(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get production by coffee type: " + coffeeType, e);
        }
        return productions;
    }

    @Override
    public CoffeeProduction getByBranchId(int branchId) throws DataAccessException {
        String sql = "SELECT * FROM CoffeeProduction WHERE BranchID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, branchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapToCoffeeProduction(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Failed to get coffee production for branch ID: " + branchId, e);
        }
        return null;
    }

    private CoffeeProduction mapToCoffeeProduction(ResultSet rs) throws SQLException {
        return new CoffeeProduction(
                rs.getInt("ProductionID"),
                rs.getInt("BranchID"),
                rs.getInt("UserID"),
                rs.getString("Supplier"),
                rs.getString("CoffeeType"),
                rs.getString("ProductType"),
                rs.getDouble("ProductionQuantitiesOfCoffee_KG"),
                rs.getDouble("Pr_CarbonEmissions_KG"),
                rs.getDate("ActivityDate")
        );
    }
}