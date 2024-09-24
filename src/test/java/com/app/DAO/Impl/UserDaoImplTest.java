package com.app.DAO.Impl;

import com.app.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private UserDaoImpl userDao;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("test")
                .email("test@gmail.com")
                .password("test")
                .build();
    }

    @Test
    public void testRegister() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        long generatedId = 1L;
        keyHolder.getKeyList().add(Map.of("GENERATED_KEY", generatedId));

        when(jdbcTemplate.update(any(PreparedStatementCreator.class),any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder passedKeyHolder = invocation.getArgument(1);
                    passedKeyHolder.getKeyList().add(Map.of("GENERATED_KEY", generatedId));
                    return 1;
                });

        User savedUser = userDao.register(user);
        assertNotNull(savedUser);
        assertEquals(generatedId,savedUser.getId());
        verify(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
    }

    @Test
    public void testFindUserById() {
        String sql = "SELECT id, username, email FROM users WHERE id = ?";
        Long id = 1L;

        when(jdbcTemplate.queryForObject(eq(sql),any(UserRowMapper.class),eq(id))).thenReturn(user);

        userDao.findUserById(user.getId());

        verify(jdbcTemplate).queryForObject(eq(sql), any(UserRowMapper.class), eq(user.getId()));
    }

    @Test
    public void testFindUserByUsername() {
        String sql = "SELECT id, username, email FROM users WHERE username = ?";
        String username = "test";

        when(jdbcTemplate.queryForObject(eq(sql),any(UserRowMapper.class),eq(username))).thenReturn(user);

        userDao.findUserByUsername(user.getUsername());

        verify(jdbcTemplate).queryForObject(eq(sql),any(UserRowMapper.class), eq(user.getUsername()));
    }

    @Test
    void testFindUserByEmail() {
        String sql = "SELECT id, username, email FROM users WHERE email = ?";
        String email = "test@gmail.com";

        when(jdbcTemplate.queryForObject(eq(sql),any(UserRowMapper.class),eq(email))).thenReturn(user);

        userDao.findUserByEmail(user.getEmail());

        verify(jdbcTemplate).queryForObject(eq(sql),any(UserRowMapper.class),eq(user.getEmail()));
    }

    @Test
    void testFindPasswordByUsername() {
        String sql = "SELECT username,password FROM users WHERE username = ?";
        String username = "test";

        when(jdbcTemplate.queryForObject(eq(sql),any(RowMapper.class),eq(username))).thenReturn(user);

        userDao.findPasswordByUsername(username);

        verify(jdbcTemplate).queryForObject(eq(sql),any(RowMapper.class),eq(user.getUsername()));
    }

    @Test
    void testUpdate() {
        String sql = "UPDATE users SET username = ?, email = ?, password = ? WHERE id = ?";

        when(jdbcTemplate.update(eq(sql),eq(user.getUsername()),eq(user.getEmail()),eq(user.getPassword()),eq(user.getId()))).thenReturn(1);
        userDao.update(user);

        verify(jdbcTemplate).update(eq(sql),eq(user.getUsername()),eq(user.getEmail()),eq(user.getPassword()),eq(user.getId()));
    }
}
