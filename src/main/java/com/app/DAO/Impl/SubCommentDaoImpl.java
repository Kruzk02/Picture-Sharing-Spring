package com.app.DAO.Impl;

import com.app.DAO.SubCommentDao;
import com.app.Model.*;
import com.app.exception.sub.SaveDataFailedException;
import com.app.exception.sub.SubCommentNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Repository
public class SubCommentDaoImpl implements SubCommentDao {

    private final JdbcTemplate template;

    @Override
    public SubComment save(SubComment subComment) {
        try {
            String sql = "INSERT INTO sub_comments (content, user_id, comment_id, media_id) VALUES(?,?,?,?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int row = template.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1,subComment.getContent());
                ps.setLong(2,subComment.getUser().getId());
                ps.setLong(3,subComment.getComment().getId());
                ps.setLong(4, subComment.getMedia().getId());
                return ps;
            },keyHolder);

            if (row > 0) {
                subComment.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
            }
            return subComment;
        } catch (DataAccessException e) {
            throw new SaveDataFailedException("Failed to save SubComment with content: " + subComment.getContent() + ". Error: " + e.getMessage());
        }
    }

    @Override
    public SubComment update(Long id, SubComment subComment) {
        StringBuilder sb = new StringBuilder("UPDATE sub_comments SET ");
        List<Object> params = new ArrayList<>();

        if (subComment.getContent() != null) {
            sb.append("content = ?, ");
            params.add(subComment.getContent());
        }

        if (subComment.getMedia().getId() != 0) {
            sb.append("media_id = ?, ");
            params.add(subComment.getMedia().getId());
        }

        if (params.isEmpty()) {
            throw new IllegalArgumentException("No fields to update");
        }

        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }

        sb.append(" WHERE id = ?");
        params.add(id);

        String sql = sb.toString();
        int rowAffected = template.update(sql, params.toArray());
        return rowAffected > 0 ? subComment : null;
    }

    @Override
    public List<SubComment> findAllByCommentId(Long commentId, SortType sortType, int limit, int offset) {
        String sql = "SELECT sc.id AS sc_id, sc.content AS sc_content, sc.create_at as sc_create_at, " +
                "sc.media_id AS sc_media_id, " +
                "sc.comment_id AS sc_comment_id, " +
                "u.id AS user_id, " +
                "u.username AS user_username, " +
                "c.content AS comment_content " +
                "FROM sub_comments sc " +
                "JOIN users u ON sc.user_id = u.id " +
                "JOIN comments c ON sc.comment_id = c.id " +
                "WHERE sc.comment_id = ? ORDER BY sc.create_at " + sortType.getOrder() + " limit ? offset ?";
        return template.query(sql, new SubCommentRowMapper(), commentId, limit, offset);
    }

    @Override
    public SubComment findById(Long id) {
        try {
            String sql = "SELECT sc.id as sc_id, sc.content AS sc_content, sc.create_at AS sc_create_at, " +
                    "sc.media_id AS sc_media_id, " +
                    "sc.comment_id AS sc_comment_id, " +
                    "u.id AS user_id, " +
                    "u.username AS user_username, " +
                    "c.content AS comment_content " +
                    "FROM sub_comments sc " +
                    "JOIN users u ON sc.user_id = u.id " +
                    "JOIN comments c ON sc.comment_id = c.id " +
                    "WHERE sc.id = ?";
            return template.queryForObject(sql, new SubCommentRowMapper(), id);
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
            throw new SubCommentNotFoundException("Sub comment not found with id: " + id);
        }
    }

    @Override
    public int deleteById(Long id) {
        try {
            String sql = "DELETE FROM sub_comments WHERE id = ?";
            return template.update(sql,id);
        }catch (DataAccessException e) {
            throw new SubCommentNotFoundException("Sub comment not found with a id: " + id);
        }
    }
}

class SubCommentRowMapper implements RowMapper<SubComment> {

    @Override
    public SubComment mapRow(ResultSet rs, int rowNum) throws SQLException {
        SubComment subComment = new SubComment();
        subComment.setId(rs.getLong("sc_id"));

        subComment.setContent(rs.getString("sc_content"));

        Media media = new Media();
        media.setId(rs.getLong("sc_media_id"));
        subComment.setMedia(media);

        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("user_username"));
        subComment.setUser(user);

        Comment comment = new Comment();
        comment.setId(rs.getLong("sc_comment_id"));
        comment.setContent(rs.getString("comment_content"));
        subComment.setComment(comment);

        subComment.setCreateAt(rs.getTimestamp("sc_create_at").toLocalDateTime());
        return subComment;
    }
}

