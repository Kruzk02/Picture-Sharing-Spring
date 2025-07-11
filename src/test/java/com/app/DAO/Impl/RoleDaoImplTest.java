package com.app.DAO.Impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.app.Model.Privilege;
import com.app.Model.Role;
import java.util.List;
import java.util.Map;
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

@ExtendWith(MockitoExtension.class)
class RoleDaoImplTest {

  @InjectMocks private RoleDaoImpl roleDao;
  @Mock private JdbcTemplate jdbcTemplate;

  private Role role;

  @BeforeEach
  void setUp() {
    role =
        Role.builder()
            .id(1L)
            .name("name")
            .privileges(List.of(Privilege.builder().id(1L).name("name").build()))
            .build();
  }

  @Test
  void create_shouldInsertRole() {
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    keyHolder.getKeyList().add(Map.of("GENERATED_KEY", 1L));

    Mockito.when(
            jdbcTemplate.update(
                Mockito.any(PreparedStatementCreator.class), Mockito.any(KeyHolder.class)))
        .thenAnswer(
            invocation -> {
              KeyHolder kh = invocation.getArgument(1);
              kh.getKeyList().add(Map.of("GENERATED_KEY", 1L));
              return 1;
            });

    var result = roleDao.create(role);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("name", result.getName());
  }

  @Test
  void create_shouldReturnNull_whenInsertFails() {
    Mockito.when(
            jdbcTemplate.update(
                Mockito.any(PreparedStatementCreator.class), Mockito.any(KeyHolder.class)))
        .thenReturn(0);

    var result = roleDao.create(role);

    assertNull(result);

    Mockito.verify(jdbcTemplate, Mockito.never())
        .batchUpdate(Mockito.anyString(), Mockito.any(BatchPreparedStatementSetter.class));
  }

  @Test
  void findByName_shouldReturnRole_whenRoleExists() {
    Mockito.when(
            jdbcTemplate.query(
                eq(
                    "SELECT r.id AS role_id, r.name AS role_name, p.id AS privilege_id, p.name AS privilege_name "
                        + "FROM roles r "
                        + "JOIN roles_privileges rp ON rp.role_id = r.id "
                        + "JOIN privileges p ON rp.privilege_id = p.id "
                        + "WHERE r.name = ?"),
                any(ResultSetExtractor.class),
                eq("name")))
        .thenReturn(role);

    var result = roleDao.findByName("name");
    assertNotNull(result);
    assertEquals(result.getName(), role.getName());
    Mockito.verify(jdbcTemplate)
        .query(
            eq(
                "SELECT r.id AS role_id, r.name AS role_name, p.id AS privilege_id, p.name AS privilege_name "
                    + "FROM roles r "
                    + "JOIN roles_privileges rp ON rp.role_id = r.id "
                    + "JOIN privileges p ON rp.privilege_id = p.id "
                    + "WHERE r.name = ?"),
            any(ResultSetExtractor.class),
            eq("name"));
  }

  @Test
  void findByName_shouldReturnNull_whenRoleDoesNotExists() {
    when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class), eq(role.getName())))
        .thenThrow(new EmptyResultDataAccessException(1));
    var result = roleDao.findByName("name");
    assertNull(result);
  }
}
