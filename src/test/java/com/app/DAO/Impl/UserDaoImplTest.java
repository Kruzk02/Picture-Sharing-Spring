package com.app.DAO.Impl;

import com.app.Model.Privilege;
import com.app.Model.Role;
import com.app.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.ResultSet;
import java.util.List;
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
                .roles(List.of(Role.builder()
                        .id(1L)
                        .name("ROLE_USER")
                        .build()))
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
        String sql = "SELECT u.id AS user_id, u.username AS user_username, u.password AS user_password, r.id AS role_id, r.name AS role_name " +
                "FROM users u " +
                "JOIN users_roles ur ON ur.user_id = u.id " +
                "JOIN roles r ON ur.role_id = r.id " +
                "WHERE u.username = ?";
        String username = "test";

        when(jdbcTemplate.query(eq(sql),any(ResultSetExtractor.class),eq(username))).thenAnswer(invocation -> {
            ResultSet rs = mock(ResultSet.class);
            when(rs.next()).thenReturn(true,false);
            when(rs.getLong("user_id")).thenReturn(1L);
            when(rs.getString("user_username")).thenReturn("test");
            when(rs.getString("user_password")).thenReturn("test");
            when(rs.getLong("role_id")).thenReturn(1L);
            when(rs.getString("role_name")).thenReturn("ROLE_USER");

            ResultSetExtractor<User> extractor = invocation.getArgument(1);
            return extractor.extractData(rs);
        });

        User result = userDao.findPasswordNRoleByUsername(username);

        assertNotNull(result);
        assertEquals(result.getId(),user.getId());
        assertEquals(result.getUsername(),user.getUsername());
        assertEquals(result.getRoles().size(),user.getRoles().size());
        verify(jdbcTemplate).query(eq(sql),any(ResultSetExtractor.class),eq(user.getUsername()));
    }

    @Test
    void testUpdate() {
        String sql = "UPDATE users SET username = ?, email = ?, password = ? WHERE id = ?";

        when(jdbcTemplate.update(eq(sql),eq(user.getUsername()),eq(user.getEmail()),eq(user.getPassword()),eq(user.getId()))).thenReturn(1);
        userDao.update(user);

        verify(jdbcTemplate).update(eq(sql),eq(user.getUsername()),eq(user.getEmail()),eq(user.getPassword()),eq(user.getId()));
    }
}
