package com.cofaktory.footprint.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class test {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/carbon_footprint_tracker";

        try (Connection conn = DriverManager.getConnection(url, "coffee_user", "1234");
             Statement stmt = conn.createStatement()) {

            System.out.println("Successfully connected to database!");

            boolean isConnected = stmt.execute("SELECT 1");
            System.out.println("Connection test query executed: " + (isConnected ? "success" : "failed"));

        } catch (SQLException e) {
            System.err.println("Connection failed!");
            e.printStackTrace();
        }
    }
}