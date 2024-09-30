package com.app.DAO.Impl;

import com.app.Model.Privilege;
import com.app.Model.Role;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleDaoImplTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @InjectMocks private RoleDaoImpl roleDao;

    private Role role;

    @BeforeEach
    void setUp() {
        role = Role.builder()
                .id(1L)
                .name("ROLE_USER")
                .privileges(List.of(Privilege.builder()
                        .id(1L)
                        .name("READ_PRIVILEGE")
                        .build()))
                .build();
    }

    @Test
    void create() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        long generatedId = 1L;
        keyHolder.getKeyList().add(Map.of("GENERATED_KEY", generatedId));

        when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder passedKeyHolder = invocation.getArgument(1);
                    passedKeyHolder.getKeyList().add(Map.of("GENERATED_KEY", generatedId));
                    return 1;
                });


        Role savedRole = roleDao.create(role);

        assertNotNull(savedRole);
        assertEquals(generatedId, savedRole.getId());
        verify(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
    }

    @Test
    void findByName() {
        String sql = "SELECT r.id AS role_id, r.name AS role_name, p.id AS privilege_id, p.name AS privilege_name " +
                "FROM roles r " +
                "JOIN roles_privileges rp ON rp.role_id = r.id " +
                "JOIN privileges p ON rp.privilege_id = p.id " +
                "WHERE r.name = ?";

        String name = "ROLE_USER";

        when(jdbcTemplate.query(eq(sql),any(ResultSetExtractor.class),eq(name))).thenAnswer(invocation -> {
            ResultSet rs = mock(ResultSet.class);
            when(rs.next()).thenReturn(true, false);
            when(rs.getLong("role_id")).thenReturn(1L);
            when(rs.getString("role_name")).thenReturn("ROLE_USER");
            when(rs.getLong("privilege_id")).thenReturn(1L);
            when(rs.getString("privilege_name")).thenReturn("READ_PRIVILEGE");

            ResultSetExtractor<Role> extractor = invocation.getArgument(1);
            return extractor.extractData(rs);
        });

        Role result = roleDao.findByName(name);

        assertNotNull(result);
        assertEquals(result.getId(), role.getId());
        assertEquals(result.getName(), role.getName());
        assertEquals(result.getPrivileges().size(), role.getPrivileges().size());
        verify(jdbcTemplate).query(eq(sql),any(ResultSetExtractor.class),eq(name));
    }
}