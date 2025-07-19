package com.app.DAO.Impl;

import static org.junit.jupiter.api.Assertions.*;

import com.app.DAO.AbstractMySQLTest;
import com.app.DAO.RoleDao;
import com.app.Model.Role;
import java.sql.PreparedStatement;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class RoleDaoImplIntegrateTest extends AbstractMySQLTest {

  private RoleDao roleDao;

  @BeforeEach
  void setUp() {
    roleDao = new RoleDaoImpl(jdbcTemplate);
  }

  @Test
  void create() {
    var savedRole = roleDao.create(Role.builder().id(1L).name("name123").build());

    assertNotNull(savedRole);
    assertEquals("name123", savedRole.getName());
  }

  @Test
  void findByName() {
    insertRoleAndPrivilege();
    var foundRole = roleDao.findByName("name123");

    assertNotNull(foundRole);
    assertEquals("name123", foundRole.getName());
  }

  private void insertRoleAndPrivilege() {
    KeyHolder roleKeyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(
        connection -> {
          PreparedStatement ps =
              connection.prepareStatement(
                  "INSERT INTO roles(name) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
          ps.setString(1, "name123");
          return ps;
        },
        roleKeyHolder);
    Long roleId = roleKeyHolder.getKey().longValue();

    KeyHolder privilegeKeyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(
        connection -> {
          PreparedStatement ps =
              connection.prepareStatement(
                  "INSERT INTO privileges(name) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
          ps.setString(1, "name123");
          return ps;
        },
        privilegeKeyHolder);
    Long privilegeId = privilegeKeyHolder.getKey().longValue();

    jdbcTemplate.update(
        "INSERT INTO roles_privileges(role_id, privilege_id) VALUES(?, ?)", roleId, privilegeId);
  }
}
