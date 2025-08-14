package com.cofaktory.footprint.dao.jdbcImpl;

import com.cofaktory.footprint.dao.NotificationDao;
import com.cofaktory.footprint.model.Notification;
import com.cofaktory.footprint.myExceptions.DataAccessException;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDaoImpl implements NotificationDao {
    private final DataSource dataSource;

    public NotificationDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Notification getById(int id) throws DataAccessException {
        String sql = "SELECT * FROM Notification WHERE NotificationID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapToNotification(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get notification by ID: " + id, e);
        }
        return null;
    }

    @Override
    public List<Notification> getAll() throws DataAccessException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM Notification ORDER BY Timestamp DESC";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                notifications.add(mapToNotification(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get all notifications", e);
        }
        return notifications;
    }

    @Override
    public boolean save(Notification notification) throws DataAccessException {
        if (notification.getNotificationId() > 0) {
            return update(notification);
        } else {
            return insert(notification);
        }
    }

    @Override
    public boolean insert(Notification notification) throws DataAccessException {
        String sql = "INSERT INTO Notification (UserID, Message, Timestamp, IsRead) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getMessage());
            stmt.setTimestamp(3, Timestamp.valueOf(
                    notification.getTimestamp() != null ?
                            notification.getTimestamp() : LocalDateTime.now()));
            stmt.setBoolean(4, notification.isRead());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        notification.setNotificationId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert notification", e);
        }
    }

    @Override
    public boolean update(Notification notification) throws DataAccessException {
        String sql = "UPDATE Notification SET UserID = ?, Message = ?, Timestamp = ?, IsRead = ? WHERE NotificationID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getMessage());
            stmt.setTimestamp(3, Timestamp.valueOf(notification.getTimestamp()));
            stmt.setBoolean(4, notification.isRead());
            stmt.setInt(5, notification.getNotificationId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update notification: " + notification.getNotificationId(), e);
        }
    }


    @Override
    public boolean delete(Notification notificationId) throws DataAccessException {
        String sql = "DELETE FROM Notification WHERE NotificationID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notificationId.getNotificationId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete notification: " + notificationId, e);
        }
    }


    @Override
    public boolean markAsRead(int notificationId) throws DataAccessException {
        String sql = "UPDATE Notification SET IsRead = TRUE WHERE NotificationID = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to mark notification as read: " + notificationId, e);
        }
    }

    @Override
    public int markAllAsRead(int userId) throws DataAccessException {
        String sql = "UPDATE Notification SET IsRead = TRUE WHERE UserID = ? AND IsRead = FALSE";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to mark all notifications as read for user ID: " + userId, e);
        }
    }

    @Override
    public int deleteAllReadNotifications(int userId) throws DataAccessException {
        String sql = "DELETE FROM Notification WHERE UserID = ? AND IsRead = TRUE";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete read notifications for user ID: " + userId, e);
        }
    }

    @Override
    public List<Notification> getNotificationsByUserId(int userId) throws DataAccessException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM Notification WHERE UserID = ? ORDER BY Timestamp DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapToNotification(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get notifications for user ID: " + userId, e);
        }
        return notifications;
    }

    @Override
    public List<Notification> getUnreadNotificationsByUserId(int userId) throws DataAccessException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM Notification WHERE UserID = ? AND IsRead = FALSE ORDER BY Timestamp DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapToNotification(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get unread notifications for user ID: " + userId, e);
        }
        return notifications;
    }

    private Notification mapToNotification(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("Timestamp");
        return new Notification(
                rs.getInt("NotificationID"),
                rs.getInt("UserID"),
                rs.getString("Message"),
                timestamp != null ? timestamp.toLocalDateTime() : null,
                rs.getBoolean("IsRead")
        );
    }

}