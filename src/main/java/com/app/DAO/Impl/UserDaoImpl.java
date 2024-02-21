package com.app.DAO.Impl;

import com.app.DAO.UserDao;
import com.app.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User register(User user) {
        String sql = "INSERT INTO users (username,email,password) VALUES (?,?,?)";
        int rowsAffected = jdbcTemplate.update(sql,
                user.getUsername(),
                user.getEmail(),
                user.getPassword());
        if(rowsAffected > 0){
            return user;
        }else{
            return null;
        }
    }

    @Override
    public User login(String username) {
        String sql = "SELECT username,password from users where username = ?";
        return jdbcTemplate.queryForObject(sql,new UserRowMapper());
    }

    @Override
    public User findUserById(Long id) {
        String sql = "SELECT * from users where id = ?";
        return jdbcTemplate.queryForObject(sql,new UserRowMapper(),id);
    }

    @Override
    public User findUserByUsername(String username) {
        String sql = "SELECT * from users where username =?";
        return jdbcTemplate.queryForObject(sql,new UserRowMapper(),username);
    }

    @Override
    public User findUserByEmail(String email) {
        String sql = "SELECT * from users where email =?";
        return jdbcTemplate.queryForObject(sql,new UserRowMapper(),email);
    }
}

class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        return user;
    }
}
