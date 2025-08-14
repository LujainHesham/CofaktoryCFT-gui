package com.cofaktory.footprint.service;

import com.cofaktory.footprint.dao.AuditLoggingDao;
import com.cofaktory.footprint.model.AuditLogging;
import com.cofaktory.footprint.myExceptions.DataAccessException;
import java.time.LocalDateTime;
import java.util.List;

public class AuditLoggingService {
    private final AuditLoggingDao auditLoggingDao;

    public AuditLoggingService(AuditLoggingDao auditLoggingDao) {
        this.auditLoggingDao = auditLoggingDao;
    }

    public void logAction(int userId, String action, String tableName, int recordId) throws DataAccessException {
        AuditLogging log = new AuditLogging(0, userId, action, tableName, recordId, LocalDateTime.now());
        auditLoggingDao.insert(log);
    }

    public List<AuditLogging> getLogsByUser(int userId) throws DataAccessException {
        return auditLoggingDao.getLogsByUserId(userId);
    }

    public List<AuditLogging> getRecentLogs() throws DataAccessException {
        return auditLoggingDao.getAll();
    }
}