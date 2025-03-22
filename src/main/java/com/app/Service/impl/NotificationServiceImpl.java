package com.app.Service.impl;

import com.app.DAO.NotificationDao;
import com.app.DAO.UserDao;
import com.app.Model.Notification;
import com.app.Model.User;
import com.app.Service.NotificationService;
import com.app.exception.sub.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationDao notificationDao;
    private final UserDao userDao;
    private final Map<Long, SseEmitter> emitters;

    private User getAuthenticationUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userDao.findUserByUsername(authentication.getName());
    }

    @Override
    public Notification save(Notification notification) {
        User user = userDao.findUserById(notification.getUserId());
        if (user == null) {
            throw new UserNotFoundException("User not found with a id: " + notification.getUserId());
        }

        if (notification.getMessage().isEmpty()) {
            throw new RuntimeException("Notification message is empty");
        }

        Notification savedNotification = notificationDao.save(notification);
        SseEmitter emitter = emitters.get(user.getId());
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(savedNotification)
                );
            } catch (IOException e) {
                emitters.remove(user.getId());
                emitter.completeWithError(e);
            }
        }

        return savedNotification;
    }

    @Override
    public List<Notification> findByUserId(int limit, int offset, Boolean fetchUnread) {
        User user = getAuthenticationUser();
        if (user == null) {
            throw new UserNotFoundException("User not found with a id: " + user.getId());
        }
        List<Notification> notifications = notificationDao.findByUserId(user.getId(), limit, offset, fetchUnread);
        if (notifications.isEmpty()) {
            return Collections.emptyList();
        }
        return notifications;
    }

    @Override
    public SseEmitter createEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        return emitter;
    }

    @Override
    public void deleteById(Long id) {
        Notification notification = notificationDao.findById(id);
        if (notification == null) {
            throw new RuntimeException("Notification not found with a id: " + id);
        }

        notificationDao.deleteById(notification.getId());
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationDao.findById(notificationId);
        if (notification == null) {
            throw new RuntimeException("Notification not found with a id: " + notificationId);
        }

        notificationDao.markAsRead(notification.getId());
    }

    @Override
    public void markAllAsRead() {
        User user = getAuthenticationUser();
        notificationDao.markAllAsRead(user.getId());
    }

    @Override
    public void deleteByUserId() {
        User user = getAuthenticationUser();
        notificationDao.deleteByUserId(user.getId());
    }
}
