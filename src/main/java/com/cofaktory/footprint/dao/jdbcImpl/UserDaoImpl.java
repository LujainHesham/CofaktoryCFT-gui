package com.cofaktory.footprint.dao.jdbcImpl;

import com.cofaktory.footprint.model.User;
import com.cofaktory.footprint.myExceptions.DataAccessException;
import com.cofaktory.footprint.myExceptions.UserNotFoundException;
import com.cofaktory.footprint.dao.UserDao;
import com.cofaktory.footprint.util.PasswordHasher;

import javax.sql.DataSource;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
// ...imports stay unchanged...

public class UserDaoImpl implements UserDao {
    private final DataSource dataSource;

    public UserDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User getById(int id) throws DataAccessException {
        String sql = "SELECT UserID, BranchID, UserName, UserRole, UserEmail, Password, Salt, ForcePasswordChange FROM user WHERE UserID = ?";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("UserID"),
                            (Integer) rs.getObject("BranchID"),
                            rs.getString("UserName"),
                            rs.getString("UserRole"),
                            rs.getString("UserEmail"),
                            rs.getString("Password"),
                            rs.getString("Salt"),
                            rs.getBoolean("ForcePasswordChange")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user", e);
        }
    }

    @Override
    public User getByEmail(String email) throws DataAccessException {
        String sql = "SELECT UserID, BranchID, UserName, UserRole, UserEmail, Password, Salt, ForcePasswordChange FROM user WHERE UserEmail = ?";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("UserID"),
                            (Integer) rs.getObject("BranchID"),
                            rs.getString("UserName"),
                            rs.getString("UserRole"),
                            rs.getString("UserEmail"),
                            rs.getString("Password"),
                            rs.getString("Salt"),
                            rs.getBoolean("ForcePasswordChange")
                    );
                }
                throw new UserNotFoundException("User not found with email: " + email);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user by email", e);
        }
    }

    @Override
    public List<User> getAll() throws DataAccessException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT UserID, BranchID, UserName, UserRole, UserEmail, Password, Salt, ForcePasswordChange FROM user";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("UserID"),
                        (Integer) rs.getObject("BranchID"),
                        rs.getString("UserName"),
                        rs.getString("UserRole"),
                        rs.getString("UserEmail"),
                        rs.getString("Password"),
                        rs.getString("Salt"),
                        rs.getBoolean("ForcePasswordChange")
                ));
            }
            return users;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving all users", e);
        }
    }

    @Override
    public boolean save(User u) throws DataAccessException {
        try {
            if (userExists(u)) {
                return update(u);
            } else {
                return insert(u);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error saving user", e);
        }
    }

    @Override
    public boolean userExists(User userId) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE UserID = ?";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId.getUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean insert(User u) throws DataAccessException {
        String sql = "INSERT INTO user (BranchID, UserName, UserRole, UserEmail, Password, Salt, ForcePasswordChange) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setObject(1, u.getBranchId(), Types.INTEGER);
            stmt.setString(2, u.getUserName());
            stmt.setString(3, u.getUserRole());
            stmt.setString(4, u.getUserEmail());
            stmt.setString(5, u.getPassword());
            stmt.setString(6, u.getSalt());
            stmt.setBoolean(7, u.isForcePasswordChange());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        u.setUserId(generatedKeys.getInt(1));
                        System.out.println("Generated User ID: " + u.getUserId());
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("SQL Error in insert user: " + e.getMessage());
            e.printStackTrace();
            throw new DataAccessException("Error inserting user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(User u) throws DataAccessException {
        String sql = "UPDATE user SET BranchID=?, UserName=?, UserRole=?, UserEmail=?, Password=?, Salt=?, ForcePasswordChange=? WHERE UserID=?";
        try (Connection conn = dataSource.getConnection()) {
            // Fetch current data
            String currentPassword;
            String currentSalt;
            boolean currentForceChange;
            String selectSql = "SELECT Password, Salt, ForcePasswordChange FROM user WHERE UserID = ?";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, u.getUserId());
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        currentPassword = rs.getString("Password");
                        currentSalt = rs.getString("Salt");
                        currentForceChange = rs.getBoolean("ForcePasswordChange");
                    } else {
                        throw new DataAccessException("User not found during update");
                    }
                }
            }

            boolean passwordChanged = !u.getPassword().equals(currentPassword);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, u.getBranchId(), Types.INTEGER);
                stmt.setString(2, u.getUserName());
                stmt.setString(3, u.getUserRole());
                stmt.setString(4, u.getUserEmail());

                if (passwordChanged) {
                    stmt.setString(5, u.getPassword());
                    stmt.setString(6, u.getSalt());
                    stmt.setBoolean(7, u.isForcePasswordChange());
                } else {
                    stmt.setString(5, currentPassword);
                    stmt.setString(6, currentSalt);
                    stmt.setBoolean(7, u.isForcePasswordChange());
                }

                stmt.setInt(8, u.getUserId());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating user", e);
        }
    }

    @Override
    public boolean delete(User u) throws DataAccessException {
        String sql = "DELETE FROM user WHERE UserID = ?";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, u.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting user", e);
        }
    }

    @Override
    public boolean authenticate(String email, String password) throws DataAccessException {
        String sql = "SELECT Password, Salt FROM user WHERE UserEmail = ?";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("Password");
                    String storedSalt = rs.getString("Salt");
                    return PasswordHasher.verifyPassword(password, storedSalt, storedHash);
                }
                return false;
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            throw new DataAccessException("Error during authentication", e);
        }
    }
}