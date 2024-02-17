package com.app.DAO.Impl;

import com.app.Model.Privilege;
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
class PrivilegeDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private PrivilegeDaoImpl privilegeDao;

    @Test
    public void testThatCreatePrivilegeCorrectSql(){
        Privilege privilege = Privilege.builder()
                .id(1L)
                .name("WRITE_PRIVILEGE")
                .build();
        privilegeDao.create(privilege);

        verify(jdbcTemplate).update(
                eq("INSERT INTO privileges (id,name) VALUES (?,?)"),
                eq(1L),eq("WRITE_PRIVILEGE"));
    }
}