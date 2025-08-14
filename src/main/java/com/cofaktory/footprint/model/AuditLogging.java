package com.cofaktory.footprint.model;

import java.time.LocalDateTime;

public class AuditLogging {
    private int logId;
    private int userId;
    private String action;
    private String tableName;
    private int recordId;
    private LocalDateTime timestamp;

    public AuditLogging() {

    }
    public AuditLogging(int logId, int userId, String action, String tableName, int recordId, LocalDateTime timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.action = action;
        this.tableName = tableName;
        this.recordId = recordId;
        this.timestamp = timestamp;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

