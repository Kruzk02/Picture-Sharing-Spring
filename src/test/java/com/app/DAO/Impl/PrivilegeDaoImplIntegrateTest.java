package com.app.DAO.Impl;

import static org.junit.jupiter.api.Assertions.*;

import com.app.DAO.AbstractMySQLTest;
import com.app.DAO.PrivilegeDao;
import com.app.Model.Privilege;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class PrivilegeDaoImplIntegrateTest extends AbstractMySQLTest {

  private PrivilegeDao privilegeDao;

  @BeforeEach
  void setUp() {
    privilegeDao = new PrivilegeDaoImpl(jdbcTemplate);
  }

  @Test
  void create() {
    var saved = privilegeDao.create(Privilege.builder().id(1L).name("name").build());

    assertNotNull(saved);
    assertEquals("name", saved.getName());
  }

  @Test
  void findByName() {
    var found = privilegeDao.findByName("name");

    assertNotNull(found);
    assertEquals("name", found.getName());
  }
}
