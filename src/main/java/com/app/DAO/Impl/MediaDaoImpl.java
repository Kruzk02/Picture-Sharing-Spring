package com.app.DAO.Impl;

import com.app.DAO.MediaDao;
import com.app.Model.Media;
import com.app.Model.MediaType;
import com.app.exception.sub.MediaNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

@Repository
@AllArgsConstructor
public class MediaDaoImpl implements MediaDao {

    private final JdbcTemplate template;

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Media save(Media media) {
        try {
            String sql = "INSERT INTO media(url, media_type) VALUES(?,?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int row = template.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, media.getUrl());
                ps.setString(2, media.getMediaType().toString());
                return ps;
            }, keyHolder);

            if (row > 0) {
                media.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
                return media;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @Override
    public Media update(Long id, Media media) {
        String sql = "UPDATE media SET url = ?, media_type = ? WHERE id = ?";
        int rowAffected = template.update(sql, media.getUrl(), media.getMediaType());
        return rowAffected > 0 ? media : null;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @Override
    public Media findById(Long id) {
        try {
            String sql = "SELECT id, url, media_type FROM media WHERE id = ?";
            return template.queryForObject(sql, (rs, rowNum) -> {
                Media media = new Media();
                media.setId(rs.getLong("id"));
                media.setUrl(rs.getString("url"));
                media.setMediaType(MediaType.valueOf(rs.getString("media_type")));
                return media;
            },id);
        } catch (DataAccessException e) {
            throw new MediaNotFoundException("Media not found with a id: " + id);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @Override
    public Media findByCommentId(Long commentId) {
        try {
            String sql = "SELECT m.id, m.url, m.media_type FROM media m " +
                         "INNER JOIN comments c ON m.id = c.media_id " +
                         "WHERE c.id = ?";
            return template.queryForObject(sql, (rs, rowNum) -> {
                Media media = new Media();
                media.setId(rs.getLong("id"));
                media.setUrl(rs.getString("url"));
                media.setMediaType(MediaType.valueOf(rs.getString("media_type")));
                return media;
            }, commentId);
        } catch (EmptyResultDataAccessException e) {
            throw new MediaNotFoundException("Media not found with comment ID: " + commentId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid media type for comment ID: " + commentId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @Override
    public int deleteById(Long id) {
        try {
            String sql = "DELETE FROM media WHERE id = ?";
            return template.update(sql, id);
        } catch (DataAccessException e) {
            throw new MediaNotFoundException("Media not found with a id: " + id);
        }
    }
}
