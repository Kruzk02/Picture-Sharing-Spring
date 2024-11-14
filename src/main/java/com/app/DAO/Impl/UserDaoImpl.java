package com.app.DAO.Impl;

import com.app.DAO.UserDao;
import com.app.Model.Gender;
import com.app.Model.Role;
import com.app.Model.User;
import com.app.exception.sub.FileNotFoundException;
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
import java.util.*;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    @Override
    public User register(User user) {
        String userSql = "INSERT INTO users (username, email, password, profilePicture, gender) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getProfilePicture());
            ps.setString(5, user.getGender().toString().toUpperCase());
            return ps;
        }, keyHolder);

        if (rowsAffected > 0) {
            user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

            assignRoleToUser(user.getId(),2);

            return user;
        } else {
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    private void assignRoleToUser(Long userId, int roleId) {
        String userRoleSql = "INSERT INTO users_roles (user_id, role_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(userRoleSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, userId);
                ps.setInt(2, roleId);
            }

            @Override
            public int getBatchSize() {
                return 1;
            }
        });
    }

    @Transactional(readOnly = true)
    @Override
    public User login(String username) {
        try {
            String sql = "SELECT u.id, u.username, u.email, u.password, roles.id as role_id, roles.name as role_name " +
                    "FROM users u " +
                    "JOIN users_roles ON u.id = users_roles.user_id " +
                    "JOIN roles ON users_roles.role_id = roles.id " +
                    "WHERE u.username = ?";

            return jdbcTemplate.queryForObject(sql, new UserRowMapper(false, false, false, false), username);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public User findUserById(Long id) {
        try {
            String sql = "SELECT id, username, email FROM users WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, new UserRowMapper(false, false, false, false), id);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public User findUserByUsername(String username) {
        try {
            String sql = "SELECT id, username, email FROM users WHERE username = ?";
            return jdbcTemplate.queryForObject(sql, new UserRowMapper(false, false, false, false), username);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public User findUserByEmail(String email) {
        try {
            String sql = "SELECT id, username, email FROM users WHERE email = ?";
            return jdbcTemplate.queryForObject(sql, new UserRowMapper(false, false, false, false), email);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public User findFullUserByUsername(String username) {
        try {
            String sql = "SELECT id, username, email, password, bio, profilePicture, gender FROM users WHERE username = ?";
            return jdbcTemplate.queryForObject(sql,new UserRowMapper(true, true, true, true), username);
        }catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public User findPasswordNRoleByUsername(String username) {
        try {
            String sql = "SELECT u.id AS user_id, u.username AS user_username, u.password AS user_password, r.id AS role_id, r.name AS role_name " +
                    "FROM users u " +
                    "JOIN users_roles ur ON ur.user_id = u.id " +
                    "JOIN roles r ON ur.role_id = r.id " +
                    "WHERE u.username = ?";

            return jdbcTemplate.query(sql, rs -> {
                User user = null;
                List<Role> roles = new ArrayList<>();

                while (rs.next()) {
                    if (user == null) {
                        user = new User();
                        user.setId(rs.getLong("user_id"));
                        user.setUsername(rs.getString("user_username"));
                        user.setPassword(rs.getString("user_password"));
                    }

                    Role role = new Role();
                    role.setId(rs.getLong("role_id"));
                    role.setName(rs.getString("role_name"));
                    roles.add(role);
                }

                if (user != null) {
                    user.setRoles(roles);
                }

                return user;
            }, username);

        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("User not found with a username: " + username);
        } catch (DataAccessException e) {
            throw new RuntimeException("An error occurred while accessing the database", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String findUserProfilePictureByUsername(String username) {
        try {
            String sql = "SELECT profilePicture FROM users WHERE username = ?";
            return jdbcTemplate.queryForObject(sql,
                    (rs, rowNum) -> rs.getString("profilePicture"),
                    username);
        } catch (EmptyResultDataAccessException e) {
            throw new FileNotFoundException("File not found with a username: " + username);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    @Override
    public User update(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, password = ?, bio = ?, profilePicture = ?, gender = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                user.getUsername(), user.getEmail(), user.getPassword(),
                user.getBio(), user.getProfilePicture(), user.getGender().toString().toLowerCase(),
                user.getId());
        if (rowsAffected > 0) {
            return findFullUserByUsername(user.getUsername());
        } else {
            return null;
        }
    }
}

class UserRowMapper implements RowMapper<User> {
    private final boolean includeProfilePicture;
    private final boolean includeBio;
    private final boolean includePassword;
    private final boolean includeGender;

    public UserRowMapper(boolean includeProfilePicture, boolean includeBio, boolean includePassword, boolean includeGender) {
        this.includeProfilePicture = includeProfilePicture;
        this.includeBio = includeBio;
        this.includePassword = includePassword;
        this.includeGender = includeGender;
    }

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));

        if (includeProfilePicture) {
            user.setProfilePicture(rs.getString("profilePicture"));
        }
        if (includeBio) {
            user.setBio(rs.getString("bio"));
        }
        if (includePassword) {
            user.setPassword(rs.getString("password"));
        }
        if (includeGender) {
            String genderString = rs.getString("gender");
            if (genderString != null) {
                user.setGender(Gender.valueOf(genderString.toUpperCase()));
            }
        }
        return user;
    }
}
