package com.app.Service;

import com.app.Model.Notification;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface NotificationService {
    Notification save(Notification notification);
    List<Notification> findByUserId(int limit, int offset, Boolean fetchUnread);
    SseEmitter createEmitter(Long userId);
    void deleteById(Long id);
    void markAsRead(Long notificationId);
    void markAllAsRead();
    void deleteByUserId();
}
