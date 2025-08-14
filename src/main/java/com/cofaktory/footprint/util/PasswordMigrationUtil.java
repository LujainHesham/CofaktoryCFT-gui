package com.cofaktory.footprint.util;

import com.cofaktory.footprint.config.DatabaseConnection;

import javax.sql.DataSource;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PasswordMigrationUtil {
    public static void main(String[] args) {
        try {
            DataSource dataSource = DatabaseConnection.getDataSource();
            migratePasswords(dataSource);
            System.out.println("Password migration completed successfully!");
        } catch (Exception e) {
            System.err.println("Error migrating passwords: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void migratePasswords(DataSource dataSource) throws SQLException, NoSuchAlgorithmException {
        String selectSql = "SELECT UserID, Password FROM User";
        String updateSql = "UPDATE User SET Password = ?, Salt = ? WHERE UserID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            ResultSet rs = selectStmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                int userId = rs.getInt("UserID");
                String plainTextPassword = rs.getString("Password");

                // Generate a new proper salt
                String newSalt = PasswordHasher.generateSalt();

                // Hash the password with the new salt
                String hashedPassword = PasswordHasher.hashPassword(plainTextPassword, newSalt);

                // Update the database
                updateStmt.setString(1, hashedPassword);
                updateStmt.setString(2, newSalt);
                updateStmt.setInt(3, userId);
                updateStmt.executeUpdate();

                count++;
                System.out.println("Migrated user ID " + userId + " (" + count + " users processed)");
            }

            System.out.println("Total users migrated: " + count);
        }
    }
}