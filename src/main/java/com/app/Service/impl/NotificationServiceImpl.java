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

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationDao notificationDao;
    private final UserDao userDao;

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

        return notificationDao.save(notification);
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
