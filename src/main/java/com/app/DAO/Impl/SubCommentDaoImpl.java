package com.app.DAO.Impl;

import com.app.DAO.SubCommentDao;
import com.app.Model.Comment;
import com.app.Model.SubComment;
import com.app.Model.User;
import com.app.exception.sub.SaveDataFailedException;
import com.app.exception.sub.SubCommentNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Repository
public class SubCommentDaoImpl implements SubCommentDao {

    private final JdbcTemplate template;

    @Override
    @Transactional
    public SubComment save(SubComment subComment) {
        try {
            String sql = "INSERT INTO sub_comments (content, user_id, comment_id) VALUES(?,?,?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int row = template.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1,subComment.getContent());
                ps.setLong(2,subComment.getUser().getId());
                ps.setLong(3,subComment.getComment().getId());

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
    @Transactional(readOnly = true)
    public List<SubComment> findAllByCommentId(Long commentId) {
        String sql = "SELECT sc.*, u.username, u.email, c.content AS comment_content " +
                "FROM sub_comments sc " +
                "JOIN users u ON sc.user_id = u.id " +
                "JOIN comments c ON sc.comment_id = c.id " +
                "WHERE sc.comment_id = ?";
        return template.query(sql, new SubCommentRowMapper(), commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public SubComment findById(Long id) {
        try {
            String sql = "SELECT sc.*, u.username, u.email, c.content AS comment_content " +
                    "FROM sub_comments sc " +
                    "JOIN users u ON sc.user_id = u.id " +
                    "JOIN comments c ON sc.comment_id = c.id " +
                    "WHERE sc.id = ?";
            return template.queryForObject(sql, new SubCommentRowMapper(), id);
        } catch (DataAccessException e) {
            throw new SubCommentNotFoundException("Sub comment not found with id: " + id);
        }
    }

    @Override
    @Transactional
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
        subComment.setId(rs.getLong("id"));
        subComment.setContent(rs.getString("content"));
        subComment.setTimestamp(rs.getTimestamp("create_at"));

        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        subComment.setUser(user);

        Comment comment = new Comment();
        comment.setId(rs.getLong("comment_id"));
        comment.setContent(rs.getString("comment_content"));
        subComment.setComment(comment);

        return subComment;
    }
}

