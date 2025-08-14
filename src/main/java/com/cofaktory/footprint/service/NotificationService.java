package com.cofaktory.footprint.service;

import com.cofaktory.footprint.dao.NotificationDao;
import com.cofaktory.footprint.model.Notification;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationService {
    private final NotificationDao notificationDao;

    public NotificationService(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    public List<Notification> getUserNotifications(int userId) throws DataAccessException {
        return notificationDao.getNotificationsByUserId(userId);
    }

    public List<Notification> getUnreadNotifications(int userId) throws DataAccessException {
        return notificationDao.getUnreadNotificationsByUserId(userId);
    }

    public boolean markAsRead(int notificationId) throws DataAccessException {
        return notificationDao.markAsRead(notificationId);
    }

    public int markAllAsRead(int userId) throws DataAccessException {
        return notificationDao.markAllAsRead(userId);
    }

    public boolean sendNotification(Notification notification) throws DataAccessException {
        return notificationDao.insert(notification);
    }

    public void sendThresholdAlert(int userId, String thresholdType, double value, int branchId) throws DataAccessException {
        String messageKey = "threshold.alert." + thresholdType;
        String localizedMessage = LanguageService.getMessage(messageKey);
        String formattedMessage = String.format(localizedMessage, branchId, value);

        Notification alert = new Notification(0, userId, formattedMessage, LocalDateTime.now(), false);
        notificationDao.insert(alert);
    }
}