package com.app.DAO;

import com.app.Model.Notification;
import java.util.List;

public interface NotificationDao {
  Notification save(Notification notification);

  List<Notification> findByUserId(Long userId, int limit, int offset, Boolean fetchUnread);

  Notification findById(Long id);

  void deleteById(Long id);

  void markAsRead(Long notificationId);

  void markAllAsRead(Long userId);

  void deleteByUserId(Long userId);
}
