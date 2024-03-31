package com.app.DAO.Impl;

import com.app.DAO.CommentDao;
import com.app.Model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Repository
public class CommentDaoImpl implements CommentDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CommentDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Comment save(Comment comment) {
        try{
            String sql = "INSERT INTO comments (content,user_id,pin_id) VALUES (?,?,?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int row = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1,comment.getContent());
                ps.setLong(2,comment.getUser().getId());
                ps.setLong(3,comment.getPin().getId());
                return ps;
            },keyHolder);

            if(row > 0){
                comment.setId(keyHolder.getKey().longValue());
                return comment;
            }else {
                return null;
            }
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public Comment findById(Long id) {
        try{
            String sql = "SELECT * FROM comments WHERE id = ?";
            return jdbcTemplate.queryForObject(sql,new CommentRowMapper(),id);
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public int deleteById(Long id) {
        try{
            String sql = "DELETE FROM comments WHERE id = ?";
            return jdbcTemplate.update(sql,id);
        }catch (Exception e){
            return 0;
        }
    }
}

class CommentRowMapper implements RowMapper<Comment>{

    @Override
    public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getLong("id"));
        comment.setContent(rs.getString("content"));

        return comment;
    }
}
