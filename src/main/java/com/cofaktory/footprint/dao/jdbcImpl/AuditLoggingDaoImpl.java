package com.cofaktory.footprint.dao.jdbcImpl;

import com.cofaktory.footprint.dao.AuditLoggingDao;
import com.cofaktory.footprint.model.AuditLogging;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLoggingDaoImpl implements AuditLoggingDao {
    private final DataSource dataSource;

    public AuditLoggingDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private AuditLogging mapResultSetToAuditLogging(ResultSet rs) throws SQLException {
        return new AuditLogging(
                rs.getInt("LogID"),
                rs.getInt("UserID"),
                rs.getString("Action"),
                rs.getString("TableName"),
                rs.getInt("RecordID"),
                rs.getTimestamp("Timestamp").toLocalDateTime()
        );
    }

    @Override
    public boolean insert(AuditLogging log) throws DataAccessException {
        String sql = "INSERT INTO AuditLogging (UserID, Action, TableName, RecordID, Timestamp) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, log.getUserId());
            stmt.setString(2, log.getAction());
            stmt.setString(3, log.getTableName());
            stmt.setInt(4, log.getRecordId());
            stmt.setTimestamp(5, Timestamp.valueOf(log.getTimestamp()));

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;  // Return true if at least one row was inserted
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert audit log", e);
        }
    }


    @Override
    public AuditLogging getById(int id) throws DataAccessException {
        String sql = "SELECT * FROM AuditLogging WHERE LogID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAuditLogging(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get audit log by ID: " + id, e);
        }
        return null;
    }

    @Override
    public List<AuditLogging> getAll() throws DataAccessException {
        List<AuditLogging> logs = new ArrayList<>();
        String sql = "SELECT * FROM AuditLogging ORDER BY Timestamp DESC";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(mapResultSetToAuditLogging(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get all audit logs", e);
        }
        return logs;
    }

    @Override
    public boolean save(AuditLogging log) throws DataAccessException {
        if (log.getLogId() > 0) {
            return update(log);
        } else {
            return insert(log);
        }
    }


    @Override
    public boolean update(AuditLogging log) throws DataAccessException {
        String sql = "UPDATE AuditLogging SET UserID = ?, Action = ?, TableName = ?, " +
                "RecordID = ?, Timestamp = ? WHERE LogID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, log.getUserId());
            stmt.setString(2, log.getAction());
            stmt.setString(3, log.getTableName());
            stmt.setInt(4, log.getRecordId());
            stmt.setTimestamp(5, Timestamp.valueOf(log.getTimestamp()));
            stmt.setInt(6, log.getLogId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update audit log: " + log.getLogId(), e);
        }
    }


    @Override
    public boolean delete(AuditLogging log) throws DataAccessException {
        String sql = "DELETE FROM AuditLogging WHERE LogID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, log.getLogId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete audit log: " + log.getLogId(), e);
        }
    }

    @Override
    public boolean deleteAllLogs() throws DataAccessException {
        String sql = "DELETE FROM AuditLogging";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            return stmt.executeUpdate(sql) > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete all audit logs", e);
        }
    }

    @Override
    public boolean deleteLogsOlderThan(LocalDateTime timestamp) throws DataAccessException {
        String sql = "DELETE FROM AuditLogging WHERE Timestamp < ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(timestamp));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete logs older than: " + timestamp, e);
        }
    }



    @Override
    public List<AuditLogging> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws DataAccessException {
        List<AuditLogging> logs = new ArrayList<>();
        String sql = "SELECT * FROM AuditLogging WHERE Timestamp BETWEEN ? AND ? ORDER BY Timestamp DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAuditLogging(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get logs by date range", e);
        }
        return logs;
    }


    private List<AuditLogging> getLogsByCondition(String condition, Object param) throws DataAccessException {
        List<AuditLogging> logs = new ArrayList<>();
        String sql = "SELECT * FROM AuditLogging WHERE " + condition + " ORDER BY Timestamp DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (param instanceof Integer) {
                stmt.setInt(1, (Integer) param);
            } else {
                stmt.setString(1, (String) param);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAuditLogging(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get logs by condition: " + condition, e);
        }
        return logs;
    }

    @Override
    public List<AuditLogging> getLogsByUserId(int userId) throws DataAccessException {
        return getLogsByCondition("UserID = ?", userId);
    }
    

    @Override
    public List<AuditLogging> getLogsByAction(String action) throws DataAccessException {
        return getLogsByCondition("Action = ?", action);
    }



}