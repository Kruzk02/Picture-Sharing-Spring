package com.app.DAO.Impl;

import com.app.Model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationDaoImplTest {

    @InjectMocks private NotificationDaoImpl notificationDao;
    @Mock private JdbcTemplate jdbcTemplate;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .id(1L)
                .userId(1L)
                .message("message")
                .isRead(false)
                .build();
    }

    @Test
    void save_shouldInsertNotification() {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        keyHolder.getKeyList().add(Map.of("GENERATED_KEY", 1L));

        Mockito.when(jdbcTemplate.update(Mockito.any(PreparedStatementCreator.class), Mockito.any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder kh = invocation.getArgument(1);
                    kh.getKeyList().add(Map.of("GENERATED_KEY", 1L));
                    return 1;
                });

        var result = notificationDao.save(notification);

        assertNotNull(result);
        assertEquals(notification, result);
    }

    @Test
    void save_shouldReturnNull_whenInsertFails() {
        Mockito.when(jdbcTemplate.update(Mockito.any(PreparedStatementCreator.class), Mockito.any(KeyHolder.class)))
                .thenReturn(0);

        var result = notificationDao.save(notification);
        assertNull(result);
    }

    @Test
    void findByUserId_shouldReturnListOfUnreadNotification() {
        Mockito.when(jdbcTemplate.query(
                Mockito.eq("SELECT id, message, is_read, created_at FROM notifications " +
                        "WHERE user_id = ? AND is_read = false ORDER BY created_at DESC LIMIT ? OFFSET ?"),
                Mockito.any(RowMapper.class),
                Mockito.eq(1L),
                Mockito.eq(10),
                Mockito.eq(0)
        )).thenReturn(List.of(notification));

        var result = notificationDao.findByUserId(1L, 10, 0, true);
        assertNotNull(result);
        assertEquals(List.of(notification), result);

        Mockito.verify(jdbcTemplate).query(
                Mockito.eq("SELECT id, message, is_read, created_at FROM notifications " +
                        "WHERE user_id = ? AND is_read = false ORDER BY created_at DESC LIMIT ? OFFSET ?"),
                Mockito.any(RowMapper.class),
                Mockito.eq(1L),
                Mockito.eq(10),
                Mockito.eq(0)
        );
    }

    @Test
    void findByUserId_shouldReturnListOfReadNotification() {
        Mockito.when(jdbcTemplate.query(
                Mockito.eq("SELECT id, message, is_read, created_at FROM notifications " +
                        "WHERE user_id = ? AND is_read = true ORDER BY created_at DESC LIMIT ? OFFSET ?"),
                Mockito.any(RowMapper.class),
                Mockito.eq(1L),
                Mockito.eq(10),
                Mockito.eq(0)
        )).thenReturn(List.of(notification));

        var result = notificationDao.findByUserId(1L, 10, 0, false);
        assertNotNull(result);
        assertEquals(List.of(notification), result);

        Mockito.verify(jdbcTemplate).query(
                Mockito.eq("SELECT id, message, is_read, created_at FROM notifications " +
                        "WHERE user_id = ? AND is_read = true ORDER BY created_at DESC LIMIT ? OFFSET ?"),
                Mockito.any(RowMapper.class),
                Mockito.eq(1L),
                Mockito.eq(10),
                Mockito.eq(0)
        );
    }

    @Test
    void findById_shouldReturnNotification_whenNotificationExists() {
        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.eq("SELECT user_id, message, is_read, created_at FROM notifications WHERE id = ?"),
                Mockito.any(RowMapper.class),
                Mockito.eq(1L)
        )).thenReturn(notification);

        var result = notificationDao.findById(1L);

        assertNotNull(result);
        assertEquals(notification, result);
    }
}