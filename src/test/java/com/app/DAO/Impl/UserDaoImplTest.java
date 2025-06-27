package com.app.DAO.Impl;

import com.app.Model.*;
import com.app.exception.sub.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDaoImplTest {

    @InjectMocks private UserDaoImpl userDao;
    @Mock private JdbcTemplate jdbcTemplate;

    private User user;
    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("username")
                .email("email@gmail.com")
                .password("HashedPassword")
                .gender(Gender.MALE)
                .media(Media.builder().id(1L).mediaType(MediaType.IMAGE).url("NO").build())
                .roles(List.of(Role.builder().id(2L).name("ROLE_USER").privileges(List.of(Privilege.builder().id(2L).name("READ").build())).build()))
                .bio("bio")
                .enable(false)
                .build();
    }

    @Test
    void register_ShouldInsertUserAndAssignRole() {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        keyHolder.getKeyList().add(Map.of("GENERATED_KEY", 1L));

        Mockito.when(jdbcTemplate.update(Mockito.any(PreparedStatementCreator.class), Mockito.any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder kh = invocation.getArgument(1);
                    kh.getKeyList().add(Map.of("GENERATED_KEY", 1L));
                    return 1;
                });

        User result = userDao.register(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("username", result.getUsername());

        Mockito.verify(jdbcTemplate).batchUpdate(Mockito.eq("INSERT INTO users_roles (user_id, role_id) VALUES (?, ?)"), Mockito.any(BatchPreparedStatementSetter.class));
    }

    @Test
    void register_ShouldReturnNull_WhenInsertFails() {
        Mockito.when(jdbcTemplate.update(Mockito.any(PreparedStatementCreator.class), Mockito.any(KeyHolder.class)))
                .thenReturn(0);

        var result = userDao.register(user);

        assertNull(result);

        Mockito.verify(jdbcTemplate, Mockito.never()).batchUpdate(Mockito.anyString(), Mockito.any(BatchPreparedStatementSetter.class));
    }

    @Test
    void login_shouldReturnUser_whenUserExists() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT u.id, u.username, u.email, u.password, u.enable, roles.id as role_id, roles.name as role_name " +
                "FROM users u " +
                "JOIN users_roles ON u.id = users_roles.user_id " +
                "JOIN roles ON users_roles.role_id = roles.id " +
                "WHERE u.username = ?"),
                any(UserRowMapper.class),
                eq(user.getUsername()))
        ).thenReturn(user);

        var result = userDao.login(user.getUsername());

        assertNotNull(result);
        assertEquals(result.getUsername(), user.getUsername());
        verify(jdbcTemplate).queryForObject(
                eq("SELECT u.id, u.username, u.email, u.password, u.enable, roles.id as role_id, roles.name as role_name " +
                "FROM users u " +
                "JOIN users_roles ON u.id = users_roles.user_id " +
                "JOIN roles ON users_roles.role_id = roles.id " +
                "WHERE u.username = ?"),
                any(UserRowMapper.class),
                eq(user.getUsername())
        );
    }

    @Test
    void login_shouldReturnNull_whenUserDoesNotExists() {
        when(jdbcTemplate.queryForObject(anyString(), any(UserRowMapper.class), eq(user.getUsername()))).thenThrow(new EmptyResultDataAccessException(1));

        var result = userDao.login(user.getUsername());
        assertNull(result);
    }

    @Test
    void findUserById_shouldReturnUser_whenUserExists() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT id, username, email, enable FROM users WHERE id = ?"),
                any(UserRowMapper.class),
                eq(1L))
        ).thenReturn(user);

        var result = userDao.findUserById(1L);
        assertNotNull(result);
        assertEquals(result.getId(), user.getId());
        verify(jdbcTemplate).queryForObject(
                eq("SELECT id, username, email, enable FROM users WHERE id = ?"),
                any(UserRowMapper.class),
                eq(1L)
        );
    }

    @Test
    void findUserById_shouldThrowException_whenUserDoesNotExists() {
        when(jdbcTemplate.queryForObject(anyString(), any(UserRowMapper.class), eq(user.getId()))).thenThrow(new EmptyResultDataAccessException(1));
        assertThrows(UserNotFoundException.class, () -> userDao.findUserById(1L));
    }

    @Test
    void findUserByUsername_shouldReturnUser_whenUserExists() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT id, username, email, enable FROM users WHERE username = ?"),
                any(UserRowMapper.class),
                eq(user.getUsername())
        )).thenReturn(user);

        var result = userDao.findUserByUsername(user.getUsername());
        assertNotNull(result);
        assertEquals(result.getUsername(), user.getUsername());

        verify(jdbcTemplate).queryForObject(
                eq("SELECT id, username, email, enable FROM users WHERE username = ?"),
                any(UserRowMapper.class),
                eq(user.getUsername())
        );
    }

    @Test
    void findUserByUsername_shouldReturnNull_whenUserDoesNotExists() {
        when(jdbcTemplate.queryForObject(anyString(), any(UserRowMapper.class), eq(user.getUsername()))).thenThrow(new EmptyResultDataAccessException(1));
        var result = userDao.findUserByUsername(user.getUsername());
        assertNull(result);
    }

    @Test
    void findUserByEmail_shouldReturnUser_whenUserExists() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT id, username, email, enable FROM users WHERE email = ?"),
                any(UserRowMapper.class),
                eq(user.getEmail())
        )).thenReturn(user);

        var result = userDao.findUserByEmail(user.getEmail());
        assertNotNull(result);
        assertEquals(result.getEmail(), user.getEmail());

        verify(jdbcTemplate).queryForObject(
                eq("SELECT id, username, email, enable FROM users WHERE email = ?"),
                any(UserRowMapper.class),
                eq(user.getEmail())
        );
    }

    @Test
    void findUserByEmail_shouldThrowException_whenUserDoesNotExists() {
        when(jdbcTemplate.queryForObject(anyString(), any(UserRowMapper.class), eq(user.getEmail()))).thenThrow(new EmptyResultDataAccessException(1));
        var result = userDao.findUserByEmail(user.getEmail());
        assertNull(result);
    }

    @Test
    void findFullUserByUsername_shouldReturnUser_whenUserExists() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT id, username, email, password, bio, media_id, enable, gender FROM users WHERE username = ?"),
                any(UserRowMapper.class),
                eq(user.getUsername())
        )).thenReturn(user);

        var result = userDao.findFullUserByUsername(user.getUsername());
        assertNotNull(result);
        assertEquals(result.getUsername(), user.getUsername());

        verify(jdbcTemplate).queryForObject(
                eq("SELECT id, username, email, password, bio, media_id, enable, gender FROM users WHERE username = ?"),
                any(UserRowMapper.class),
                eq(user.getUsername())
        );
    }

    @Test
    void findFullUserByUsername_shouldThrowException_whenUserDoesNotExists() {
        when(jdbcTemplate.queryForObject(anyString(), any(UserRowMapper.class), eq(user.getUsername()))).thenThrow(new EmptyResultDataAccessException(1));
        assertThrows(UserNotFoundException.class, () -> userDao.findFullUserByUsername(user.getUsername()));
    }

    @Test
    void findPasswordNRoleByUsername_shouldReturnUser_whenUserExists() {
        when(jdbcTemplate.query(
                eq("SELECT u.id AS user_id, u.username AS user_username, u.password AS user_password, r.id AS role_id, r.name AS role_name " +
                        "FROM users u " +
                        "JOIN users_roles ur ON ur.user_id = u.id " +
                        "JOIN roles r ON ur.role_id = r.id " +
                        "WHERE u.username = ?"),
                any(ResultSetExtractor.class),
                eq(user.getUsername())
        )).thenReturn(user);

        var result = userDao.findPasswordNRoleByUsername(user.getUsername());
        assertNotNull(result);
        assertEquals(result.getUsername(), user.getUsername());
        assertEquals(result.getPassword(), user.getPassword());

        verify(jdbcTemplate).query(
                eq("SELECT u.id AS user_id, u.username AS user_username, u.password AS user_password, r.id AS role_id, r.name AS role_name " +
                        "FROM users u " +
                        "JOIN users_roles ur ON ur.user_id = u.id " +
                        "JOIN roles r ON ur.role_id = r.id " +
                        "WHERE u.username = ?"),
                any(ResultSetExtractor.class),
                eq(user.getUsername())
        );
    }

    @Test
    void findPasswordNRoleByUsername_shouldThrowException_whenUserDoesNotExists() {
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class), eq(user.getUsername()))).thenThrow(new EmptyResultDataAccessException(1));
        assertThrows(UserNotFoundException.class, () -> userDao.findPasswordNRoleByUsername(user.getUsername()));
    }

    @Test
    void checkAccountVerifyById_shouldReturnTrue_whenUserDoesNotEnable() {
        when(jdbcTemplate.queryForObject(
                eq("SELECT enable FROM users WHERE id = ?"),
                any(RowMapper.class),
                eq(1L)
        )).thenReturn(true);

        var result = userDao.checkAccountVerifyById(user.getId());
        assertTrue(result);

        verify(jdbcTemplate).queryForObject(
                eq("SELECT enable FROM users WHERE id = ?"),
                any(RowMapper.class),
                eq(1L)
        );
    }

    @Test
    void checkAccountVerifyById_shouldThrowException_whenUserDoesNotExists() {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(user.getId()))).thenThrow(new EmptyResultDataAccessException(1));
        assertThrows(UserNotFoundException.class, () -> userDao.checkAccountVerifyById(user.getId()));
    }
}