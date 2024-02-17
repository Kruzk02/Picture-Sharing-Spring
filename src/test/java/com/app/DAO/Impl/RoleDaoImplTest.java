package com.app.DAO.Impl;

import com.app.Model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoleDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private RoleDaoImpl roleDao;

    @Test
    public void testThatCreateRoleCorrectSql(){
        Role role = Role.builder()
                .id(1L)
                .name("ROLE_ADMIN")
                .build();
        roleDao.create(role);

        verify(jdbcTemplate).update(
                eq("INSERT INTO roles (id,name) VALUES (?,?)"),
                eq(1L),eq("ROLE_ADMIN")
        );
    }
}