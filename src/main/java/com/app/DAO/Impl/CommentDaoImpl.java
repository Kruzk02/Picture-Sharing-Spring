package com.app.DAO.Impl;

import com.app.DAO.CommentDao;
import com.app.DAO.PinDao;
import com.app.DAO.UserDao;
import com.app.Model.Comment;
import com.app.exception.sub.CommentNotFoundException;
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
import java.util.List;

@Repository
public class CommentDaoImpl implements CommentDao {

    private final JdbcTemplate jdbcTemplate;
    private final UserDao userDao;
    private final PinDao pinDao;

    @Autowired
    public CommentDaoImpl(JdbcTemplate jdbcTemplate, UserDao userDao, PinDao pinDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDao = userDao;
        this.pinDao = pinDao;
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
            return jdbcTemplate.queryForObject(sql,new CommentRowMapper(userDao,pinDao),id);
        }catch (Exception e){
            throw new CommentNotFoundException("Comment not found with a id: " + id);
        }
    }

    @Override
    public List<Comment> findByPinId(Long pinId) {
        try{
            String sql = "SELECT * FROM comments WHERE pin_id = ?";
            return jdbcTemplate.query(sql, new CommentRowMapper(userDao, pinDao), pinId);
        }catch (Exception e){
            throw new CommentNotFoundException("Comment not found with a pin id: " + pinId);
        }
    }

    @Override
    public int deleteById(Long id) {
        try{
            String sql = "DELETE FROM comments WHERE id = ?";
            return jdbcTemplate.update(sql,id);
        }catch (Exception e){
            throw new CommentNotFoundException("Comment not found with a id: " + id);
        }
    }
}

class CommentRowMapper implements RowMapper<Comment>{
    private final UserDao userDao;
    private final PinDao pinDao;

    public CommentRowMapper(UserDao userDao, PinDao pinDao) {
        this.userDao = userDao;
        this.pinDao = pinDao;
    }

    @Override
    public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getLong("id"));
        comment.setContent(rs.getString("content"));
        comment.setUser(userDao.findUserById(rs.getLong("user_id")));
        comment.setPin(pinDao.findById(rs.getLong("pin_id")));
        return comment;
    }
}
