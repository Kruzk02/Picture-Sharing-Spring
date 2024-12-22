package com.app.DAO.Impl;

import com.app.DAO.CommentDao;
import com.app.Model.Comment;
import com.app.exception.sub.CommentNotFoundException;
import com.app.exception.sub.PinNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CommentDaoImpl implements CommentDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CommentDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Comment save(Comment comment) {
        try{
            String sql = "INSERT INTO comments (content,user_id,pin_id, media_id) VALUES (?,?,?,?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int row = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1,comment.getContent());
                ps.setLong(2,comment.getUserId());
                ps.setLong(3,comment.getPinId());
                ps.setLong(4, comment.getMediaId());
                return ps;
            },keyHolder);

            if(row > 0){
                comment.setId(keyHolder.getKey().longValue());
                return comment;
            }else {
                return null;
            }
        }catch (DataAccessException e){
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @Override
    public Comment update(Long id, Comment comment) {
        StringBuilder sb = new StringBuilder("UPDATE comments SET ");
        List<Object> params = new ArrayList<>();

        if (comment.getContent() != null) {
            sb.append("content = ?, ");
            params.add(comment.getContent());
        }

        if (comment.getMediaId() != 0) {
            sb.append("media_id = ?, ");
            params.add(comment.getMediaId());
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
        int rowAffected = jdbcTemplate.update(sql, params.toArray());
        return rowAffected > 0 ? comment : null;
    }

    @Transactional(readOnly = true)
    @Override
    public Comment findBasicById(Long id) {
        try {
            String sql = "SELECT id, user_id, pin_id FROM comments WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, new CommentRowMapper(false, false, true), id);
        }catch (DataAccessException e) {
            throw new CommentNotFoundException("Comment not found with a id: " + id);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Comment findDetailsById(Long id) {
        try{
            String sql = "SELECT * FROM comments WHERE id = ?";
            return jdbcTemplate.queryForObject(sql,new CommentRowMapper(true, true, true),id);
        }catch (DataAccessException e){
            throw new CommentNotFoundException("Comment not found with a id: " + id);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Comment> findByPinId(Long pinId, int limit, int offset) {
        try{
            String sql = "SELECT id, content, user_id, media_id FROM comments WHERE pin_id = ? limit ? offset ?";
            return jdbcTemplate.query(sql, new CommentRowMapper(true,true, false), pinId, limit, offset);
        }catch (DataAccessException e){
            throw new CommentNotFoundException("Comment not found with a pin id: " + pinId);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Comment> findNewestByPinId(Long pinId, int limit, int offset) {
        try {
            String sql = "SELECT id, content, user_id, media_id FROM comments WHERE pin_id = ? ORDER BY created_at DESC limit ?";
            return jdbcTemplate.query(sql, new CommentRowMapper(true, true, false),pinId,limit);
        }catch (DataAccessException e) {
            throw new CommentNotFoundException("Comment not found with a pinId: " + pinId);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<Comment> findOldestByPinId(Long pinId, int limit, int offset) {
        try {
            String sql = "SELECT id, content, user_id, media_id FROM comments WHERE pin_id = ? ORDER BY created_at ASC limit ?";
            return jdbcTemplate.query(sql, new CommentRowMapper(true, true, false),pinId,limit);
        }catch (DataAccessException e) {
            throw new CommentNotFoundException("Comment not found with a pinId: " + pinId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @Override
    public int deleteById(Long id) {
        try{
            String sql = "DELETE FROM comments WHERE id = ?";
            return jdbcTemplate.update(sql,id);
        }catch (DataAccessException e){
            throw new CommentNotFoundException("Comment not found with a id: " + id);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @Override
    public int deleteByPinId(long pinId) {
        try {
            String sql = "DELETE FROM comments WHERE pin_id = ?";
            return jdbcTemplate.update(sql, pinId);
        }catch (DataAccessException e) {
            throw new PinNotFoundException("Pin not found with a id: " + pinId);
        }
    }
}

class CommentRowMapper implements RowMapper<Comment>{

    private final boolean includeContent;
    private final boolean includeMediaId;
    private final boolean includePinId;

    CommentRowMapper(boolean includeContent, boolean includeMediaId, boolean includePinId) {
        this.includeContent = includeContent;
        this.includeMediaId = includeMediaId;
        this.includePinId = includePinId;
    }

    @Override
    public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getLong("id"));
        if (includeContent) {
            comment.setContent(rs.getString("content"));
        }

        comment.setUserId(rs.getLong("user_id"));
        if (includePinId) {
            comment.setPinId(rs.getLong("pin_id"));
        }

        if (includeMediaId) {
            comment.setMediaId(rs.getLong("media_id"));
        }

        return comment;
    }
}
