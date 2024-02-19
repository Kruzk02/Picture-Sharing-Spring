package com.app.DAO.Impl;

import com.app.Model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private UserDaoImpl userDao;

    @Test
    public void testRegister() {
        String sql = "INSERT INTO users (username,email,password) VALUES (?,?,?)";
        User user = User.builder()
                .id(1L)
                .username("test")
                .email("test@gmail.com")
                .password("test")
                .build();
        userDao.register(user);

        verify(jdbcTemplate).update(eq(sql), any(Object[].class));
    }

    @Test
    public void testLogin() {
        String sql = "SELECT username,password from users where username = ?";
        User user = User.builder()
                .id(1L)
                .username("test")
                .email("test@gmail.com")
                .password("test")
                .build();

        userDao.login(user);

        verify(jdbcTemplate).queryForObject(eq(sql), any(UserRowMapper.class));
    }

    @Test
    public void testFindUserById() {
        String sql = "SELECT * from users where id = ?";
        User user = User.builder()
                .id(1L)
                .username("test")
                .email("test@gmail.com")
                .password("test")
                .build();

        userDao.findUserById(user.getId());

        verify(jdbcTemplate).queryForObject(eq(sql), any(UserRowMapper.class), eq(user.getId()));
    }

    @Test
    public void testFindUserByUsername() {
        String sql = "SELECT * from users where username =?";
        User user = User.builder()
                .id(1L)
                .username("test")
                .email("test@gmail.com")
                .password("test")
                .build();

        userDao.findUserByUsername(user.getUsername());

        verify(jdbcTemplate).queryForObject(eq(sql)
                ,any(UserRowMapper.class), eq(user.getUsername()));
    }
}
