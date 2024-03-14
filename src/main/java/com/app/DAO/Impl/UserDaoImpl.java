package com.app.DAO.Impl;

import com.app.DAO.UserDao;
import com.app.Model.User;
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
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of UserDao using Spring JDBC for data access.
 */
@Repository
public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User register(User user) {
        String userSql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        System.out.println("MySql: "+userSql);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            return ps;
        }, keyHolder);

        if (rowsAffected > 0) {
            user.setId(keyHolder.getKey().longValue());
            Number lastInsertedId = keyHolder.getKey();

            String userRoleSql = "INSERT INTO users_roles(user_id,role_id) VALUES (?,?)";
            jdbcTemplate.update(userRoleSql,lastInsertedId,2);

            return user;
        } else {
            return null;
        }
    }

    @Override
    public User login(String username) {
        String sql = "SELECT u.id, u.username, u.email, u.password, roles.id as role_id, roles.name as role_name " +
                "FROM Users u " +
                "JOIN users_roles ON u.id = users_roles.user_id " +
                "JOIN roles ON users_roles.role_id = roles.id " +
                "WHERE u.username = ?";
        return jdbcTemplate.queryForObject(sql, new UserRowMapper(), username);
    }

    @Override
    public User findUserById(Long id) {
        try{
            String sql = "SELECT id, username, email, password from users where id = ?";
            return jdbcTemplate.queryForObject(sql,new UserRowMapper(),id);
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public User findUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), username);

        if (!users.isEmpty()) {
            return users.get(0);
        } else {
            return null;
        }
    }

    @Override
    public User findUserByEmail(String email) {
        try {
            String sql = "SELECT id, username, email, password from users where email =?";
            return jdbcTemplate.queryForObject(sql, new UserRowMapper(), email);
        }catch (Exception e){
            return null;
        }
    }
}

/**
 * RowMapper implementation to map ResultSet rows to User object.
 */
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
