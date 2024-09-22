package com.app.DAO.Impl;

import com.app.DAO.UserDao;
import com.app.Model.User;
import com.app.exception.sub.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    @Override
    public User register(User user) {
        String userSql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            return ps;
        }, keyHolder);

        if (rowsAffected > 0) {
            user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

            String userRoleSql = "INSERT INTO users_roles (user_id, role_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(userRoleSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, user.getId());
                    ps.setInt(2, 2);
                }

                @Override
                public int getBatchSize() {
                    return 1;
                }
            });

            return user;
        } else {
            return null;
        }
    }

    @Override
    public User login(String username) {
        try {
            String sql = "SELECT u.id, u.username, u.email, u.password, roles.id as role_id, roles.name as role_name " +
                    "FROM users u " +
                    "JOIN users_roles ON u.id = users_roles.user_id " +
                    "JOIN roles ON users_roles.role_id = roles.id " +
                    "WHERE u.username = ?";

            return jdbcTemplate.queryForObject(sql, new UserRowMapper(), username);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("User not found with username: " + username);
        }
    }

    @Override
    public User findUserById(Long id) {
        try {
            String sql = "SELECT id, username, email FROM users WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
    }

    @Override
    public User findUserByUsername(String username) {
        try {
            String sql = "SELECT id, username, email FROM users WHERE username = ?";
            return jdbcTemplate.queryForObject(sql, new UserRowMapper(), username);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("User not found with a username: " + username);
        }
    }

    @Override
    public User findUserByEmail(String email) {
        try {
            String sql = "SELECT id, username, email FROM users WHERE email = ?";
            return jdbcTemplate.queryForObject(sql, new UserRowMapper(), email);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("User not found with a email: " + email);
        }
    }

    @Override
    public User findPasswordByUsername(String username) {
        try {
            String sql = "SELECT username,password FROM users WHERE username = ?";
            return jdbcTemplate.queryForObject(sql,(rs, rowNum) -> {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                return user;
            },username);
        }catch (DataAccessException e) {
            throw new UserNotFoundException("User not found with a username: " + username);
        }
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, password = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, user.getUsername(), user.getEmail(), user.getPassword(), user.getId());
        if (rowsAffected > 0) {
            return findUserById(user.getId());
        } else {
            return null;
        }
    }
}

class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        return user;
    }
}
