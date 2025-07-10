package com.app.Service.impl;

import com.app.DAO.NotificationDao;
import com.app.DAO.UserDao;
import com.app.Model.Gender;
import com.app.Model.Notification;
import com.app.Model.User;
import com.app.exception.sub.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationDao notificationDao;
    @Mock private UserDao userDao;
    @Mock Map<Long, SseEmitter> sseEmitters;

    @InjectMocks NotificationServiceImpl notificationService;

    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("username")
                .email("email@gmail.com")
                .password("encodedPassword")
                .gender(Gender.OTHER)
                .build();

        notification = Notification.builder()
                .id(1L)
                .message("message")
                .userId(1L)
                .isRead(false)
                .build();
    }

    @Test
    void save_shouldSaveNotification() {
        Mockito.when(userDao.findUserById(1L)).thenReturn(user);
        Mockito.when(notificationDao.save(Mockito.argThat(n -> n.getUserId() != null && n.getMessage() != null))).thenAnswer(i -> i.getArgument(0));

        var result = notificationService.save(notification);

        assertNotNull(result);
        assertEquals(notification, result);
    }

    @Test
    void save_shouldThrowException_whenUserNotFound() {
        Mockito.when(userDao.findUserById(1L)).thenReturn(null);
        assertThrows(UserNotFoundException.class, () -> notificationService.save(notification));
    }

    @Test
    void findById_shouldReturnListOfNotification() {

        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("username");
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(userDao.findUserByUsername("username")).thenReturn(user);
        Mockito.when(notificationDao.findByUserId(1L, 10, 0, true)).thenReturn(List.of(notification));

        var result = notificationService.findByUserId(10, 0, true);

        assertEquals(List.of(notification), result);
    }

    @Test
    void createEmitter_idk() {
        var emitter = notificationService.createEmitter(1L);
        assertNotNull(emitter);
    }

    @Test
    void deleteById_shouldDeleteNotification() {
        Mockito.when(notificationDao.findById(1L)).thenReturn(notification);

        notificationService.deleteById(1L);

        Mockito.verify(notificationDao).deleteById(1L);
    }

    @Test
    void markAsRead_shouldSuccessMark() {
        Mockito.when(notificationDao.findById(1L)).thenReturn(notification);

        notificationService.markAsRead(1L);

        Mockito.verify(notificationDao).markAsRead(1L);
    }

    @Test
    void markAllAsRead_shouldSuccessMark() {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("username");
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(userDao.findUserByUsername("username")).thenReturn(user);

        notificationService.markAllAsRead();

        Mockito.verify(notificationDao).markAllAsRead(1L);
    }

    @Test
    void deleteByUserId_shouldSuccessDelete() {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("username");
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(userDao.findUserByUsername("username")).thenReturn(user);

        notificationService.deleteByUserId();

        Mockito.verify(notificationDao).deleteByUserId(1L);
    }
}
