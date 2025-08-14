package com.cofaktory.footprint.dao;

import com.cofaktory.footprint.model.Notification;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import java.util.List;

public interface NotificationDao extends DAO<Notification> {


    List<Notification> getNotificationsByUserId(int userId) throws DataAccessException; ;


    List<Notification> getUnreadNotificationsByUserId(int userId)throws DataAccessException; ;

    public boolean markAsRead(int notificationId) throws DataAccessException;
    public int deleteAllReadNotifications(int userId) throws DataAccessException;
    public int markAllAsRead(int userId) throws DataAccessException;
}