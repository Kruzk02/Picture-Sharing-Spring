package com.app.DAO.Impl;

import com.app.DAO.NotificationDao;
import com.app.Model.Notification;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class NotificationDaoImpl implements NotificationDao {

  private final JdbcTemplate template;

  @Override
  public Notification save(Notification notification) {
    try {
      String sql = "INSERT INTO notifications(user_id, message) VALUES(?, ?)";
      KeyHolder keyHolder = new GeneratedKeyHolder();

      int row =
          template.update(
              conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, notification.getUserId());
                ps.setString(2, notification.getMessage());
                return ps;
              },
              keyHolder);

      if (row > 0) {
        notification.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return notification;
      } else {
        return null;
      }
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public List<Notification> findByUserId(Long userId, int limit, int offset, Boolean fetchUnread) {
    String sql;
    if (Boolean.TRUE.equals(fetchUnread)) {
      sql =
          "SELECT id, message, is_read, created_at FROM notifications "
              + "WHERE user_id = ? AND is_read = false ORDER BY created_at DESC LIMIT ? OFFSET ?";
    } else {
      sql =
          "SELECT id, message, is_read, created_at FROM notifications "
              + "WHERE user_id = ? AND is_read = true ORDER BY created_at DESC LIMIT ? OFFSET ?";
    }

    return template.query(
        sql,
        (rs, rowNum) ->
            Notification.builder()
                .id(rs.getLong("id"))
                .userId(userId)
                .message(rs.getString("message"))
                .isRead(rs.getBoolean("is_read"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build(),
        userId,
        limit,
        offset);
  }

  @Override
  public Notification findById(Long id) {
    String sql = "SELECT user_id, message, is_read, created_at FROM notifications WHERE id = ?";
    return template.queryForObject(
        sql,
        (rs, rowNum) ->
            Notification.builder()
                .id(id)
                .userId(rs.getLong("user_id"))
                .message(rs.getString("message"))
                .isRead(rs.getBoolean("is_read"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build(),
        id);
  }

  @Override
  public void deleteById(Long id) {
    try {
      String sql = "DELETE FROM notifications WHERE id = ?";
      template.update(sql, id);
    } catch (EmptyResultDataAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void markAsRead(Long notificationId) {
    try {
      String sql = "UPDATE notifications SET is_read = true WHERE id = ?";
      template.update(sql, notificationId);
    } catch (EmptyResultDataAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void markAllAsRead(Long userId) {
    try {
      String sql = "UPDATE notifications SET is_read = true WHERE user_id = ?";
      template.update(sql, userId);
    } catch (EmptyResultDataAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteByUserId(Long userId) {
    try {
      String sql = "DELETE FROM notification WHERE user_id = ?";
      template.update(sql, userId);
    } catch (EmptyResultDataAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
