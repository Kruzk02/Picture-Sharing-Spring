package com.app.Service;

import com.app.Model.Notification;

import java.util.List;

public interface NotificationService {
    Notification save(Notification notification);
    List<Notification> findByUserId(int limit, int offset, Boolean fetchUnread);
    void deleteById(Long id);
    void markAsRead(Long notificationId);
    void markAllAsRead();
    void deleteByUserId();
}
