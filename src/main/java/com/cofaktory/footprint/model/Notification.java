package com.cofaktory.footprint.model;

import java.time.LocalDateTime;

public class Notification {

    private int notificationId;
    private int userId;
    private String message;
    private LocalDateTime timestamp;
    private boolean isRead;
    public Notification(){}
    public Notification(int notificationId, int userId, String message, LocalDateTime timestamp, boolean isRead) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }
    public Notification(int userId, String message, LocalDateTime timestamp, boolean isRead) {
        this.userId = userId;
    }
    // Getters and Setters
    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}