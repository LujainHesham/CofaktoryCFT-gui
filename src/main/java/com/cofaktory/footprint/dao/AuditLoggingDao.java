package com.cofaktory.footprint.dao;

import com.cofaktory.footprint.model.AuditLogging;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLoggingDao extends DAO<AuditLogging> {

    // Retrieval Methods
    List<AuditLogging> getLogsByUserId(int userId) throws DataAccessException;
    List<AuditLogging> getLogsByAction(String action) throws DataAccessException;
    List<AuditLogging> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws DataAccessException;

    // Deletion Methods
    boolean deleteAllLogs() throws DataAccessException;
    boolean deleteLogsOlderThan(LocalDateTime timestamp) throws DataAccessException;


}