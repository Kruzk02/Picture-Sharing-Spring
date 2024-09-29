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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrivilegeDaoImplTest {
    @Mock private JdbcTemplate template;
    @InjectMocks private PrivilegeDaoImpl privilegeDao;

    private Privilege privilege;

    @BeforeEach
    void setUp() {
        privilege = Privilege.builder()
                .id(1L)
                .name("READ")
                .build();
    }

    @Test
    void create() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        long generatedId = 1L;
        keyHolder.getKeyList().add(Map.of("GENERATED_KEY", generatedId));

        when(template.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder passedKeyHolder = invocation.getArgument(1);
                    passedKeyHolder.getKeyList().add(Map.of("GENERATED_KEY", generatedId));
                    return 1;
                });


        Privilege savedPrivilege = privilegeDao.create(privilege);

        assertNotNull(savedPrivilege);
        assertEquals(generatedId, savedPrivilege.getId());
        verify(template).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
    }

    @Test
    void findByName() {
        String sql = "SELECT * FROM privileges WHERE name = ?";
        String name = "READ";

        when(template.queryForObject(eq(sql),any(RowMapper.class),eq(name))).thenReturn(privilege);

        Privilege result = privilegeDao.findByName(name);

        assertNotNull(result);
        assertEquals(result,privilege);
        verify(template).queryForObject(eq(sql),any(RowMapper.class),eq(name));
    }
}