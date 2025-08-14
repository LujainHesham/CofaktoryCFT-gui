package com.cofaktory.footprint.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseConnection {
    private  static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static final HikariDataSource dataSource;

    static {
        try {
            // Load database configuration with provided defaults if environment variables are not set.
            String dbUrl = getEnv("DB_URL", "jdbc:mysql://localhost:3306/carbon_footprint_tracker?useSSL=false&serverTimezone=UTC");
            String username = getEnv("DB_USER", "coffee_user");
            String password = getEnv("DB_PASS", "1234");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            testConnection(); // Verify connection upon initialization
        } catch (Exception e) {
            logger.error("Database initialization failed", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            logger.warn("Using default value for {}: {}", name, defaultValue);
            return defaultValue;
        }
        return value;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void testConnection() throws SQLException {
        try (Connection conn = getConnection()) {
            logger.info("Database connection successful");
            logger.info("URL: {}", conn.getMetaData().getURL());
            logger.info("User: {}", conn.getMetaData().getUserName());
        }
    }

    public static void shutdown() {
        if (!dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool shutdown");
        }
    }
    public static javax.sql.DataSource getDataSource() {
        return dataSource;
    }
}