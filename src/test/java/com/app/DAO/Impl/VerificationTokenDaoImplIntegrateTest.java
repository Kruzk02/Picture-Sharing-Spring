package com.app.DAO.Impl;

import static org.junit.jupiter.api.Assertions.*;

import com.app.DAO.AbstractMySQLTest;
import com.app.DAO.VerificationTokenDao;
import com.app.Model.VerificationToken;
import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VerificationTokenDaoImplIntegrateTest extends AbstractMySQLTest {

  private VerificationTokenDao verificationTokenDao;

  @BeforeEach
  void setUp() {
    verificationTokenDao = new VerificationTokenDaoImpl(jdbcTemplate);
  }

  @Test
  @Order(1)
  void create() {
    var keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(conn -> {
      var ps = conn.prepareStatement("INSERT INTO users (id, username, email, password) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, 1L);
      ps.setString(2, "username");
      ps.setString(3, "email@gmail.com");
      ps.setString(4, "password");
        return ps;
        }, keyHolder);

    var result =
        verificationTokenDao.create(
            VerificationToken.builder()
                .token("token")
                .userId(keyHolder.getKey().longValue())
                .expireDate(Date.valueOf(LocalDate.now().plusDays(1)))
                .build());

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("token", result.getToken());
    assertEquals(1L, result.getUserId());
  }

  @Test
  @Order(2)
  void findByToken() {
    var result = verificationTokenDao.findByToken("token");

    assertNotNull(result);
    assertEquals("token", result.getToken());
    assertEquals(1L, result.getUserId());
  }

  @Test
  @Order(3)
  void deleteByToken() {
    var result = verificationTokenDao.deleteByToken("token");

    assertEquals(1, result);
  }
}
