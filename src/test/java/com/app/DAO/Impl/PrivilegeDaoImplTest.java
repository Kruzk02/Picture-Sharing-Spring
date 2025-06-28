package com.app.DAO.Impl;

import com.app.Model.Privilege;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PrivilegeDaoImplTest {

    @InjectMocks private PrivilegeDaoImpl privilegeDao;
    @Mock private JdbcTemplate jdbcTemplate;

    private Privilege privilege;

    @BeforeEach
    void setUp() {
        privilege = Privilege.builder()
                .id(1L)
                .name("name")
                .build();
    }

    @Test
    void create_shouldInsertPrivilege() {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        keyHolder.getKeyList().add(Map.of("GENERATED_KEY", 1L));

        Mockito.when(jdbcTemplate.update(Mockito.any(PreparedStatementCreator.class), Mockito.any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder kh = invocation.getArgument(1);
                    kh.getKeyList().add(Map.of("GENERATED_KEY", 1L));
                    return 1;
                });

        var result = privilegeDao.create(privilege);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("name", result.getName());
    }

    @Test
    void create_shouldReturnNull_whenInsertFail() {
        Mockito.when(jdbcTemplate.update(Mockito.any(PreparedStatementCreator.class), Mockito.any(KeyHolder.class)))
                .thenReturn(0);

        var result = privilegeDao.create(privilege);

        assertNull(result);

        Mockito.verify(jdbcTemplate).update(Mockito.any(PreparedStatementCreator.class), Mockito.any(KeyHolder.class));
    }

    @Test
    void findByName_shouldReturnPrivilege_whenPrivilegeExists() {
        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.eq("SELECT * FROM privileges WHERE name = ?"),
                Mockito.any(RowMapper.class),
                Mockito.eq("name"))
        ).thenReturn(privilege);

        var result = privilegeDao.findByName("name");

        assertNotNull(result);
        assertEquals(result.getName(), privilege.getName());

        Mockito.verify(jdbcTemplate).queryForObject(
                Mockito.eq("SELECT * FROM privileges WHERE name = ?"),
                Mockito.any(RowMapper.class),
                Mockito.eq("name")
        );
    }

    @Test
    void findByName_shouldReturnNull_whenPrivilegeDoesNotExists() {
        Mockito.when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.any(RowMapper.class),
                Mockito.eq("name"))
        ).thenThrow(new EmptyResultDataAccessException(1));

        var result = privilegeDao.findByName("name");
        assertNull(result);
    }
}