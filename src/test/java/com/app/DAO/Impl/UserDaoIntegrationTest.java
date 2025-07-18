package com.app.DAO.Impl;

import static org.junit.jupiter.api.Assertions.*;

import com.app.DAO.UserDao;
import com.app.Model.*;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class UserDaoIntegrationTest {

  @Container static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8");

  private JdbcTemplate jdbcTemplate;
  private UserDao userDao;

  private User user;

  @BeforeEach
  void setUp() throws Exception {
    DataSource dataSource =
        DataSourceBuilder.create()
            .url(mysql.getJdbcUrl())
            .username(mysql.getUsername())
            .password(mysql.getPassword())
            .driverClassName(mysql.getDriverClassName())
            .build();

    jdbcTemplate = new JdbcTemplate(dataSource);
    userDao = new UserDaoImpl(jdbcTemplate);

    jdbcTemplate.execute(
        "CREATE TABLE IF NOT EXISTS media("
            + "id INT AUTO_INCREMENT PRIMARY KEY,"
            + "url VARCHAR(500) NOT NULL,"
            + "media_type ENUM('VIDEO', 'IMAGE') NOT NULL,"
            + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
            + ")");


    jdbcTemplate.execute(
        "CREATE TABLE IF NOT EXISTS users ("
            + "id INT AUTO_INCREMENT PRIMARY KEY,"
            + "username VARCHAR(255) NOT NULL,"
            + "email VARCHAR(255) NOT NULL UNIQUE,"
            + "password VARCHAR(255) NOT NULL,"
            + "bio TEXT,"
            + "gender ENUM('male', 'female', 'other') NOT NULL,"
            + "enable BOOLEAN,"
            + "media_id INT,"
            + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY (media_id) REFERENCES media(id) ON DELETE CASCADE"
            + ")");
    jdbcTemplate.execute(
        "CREATE TABLE IF NOT EXISTS roles ("
            + "id int auto_increment primary key,"
            + "name VARCHAR(255)"
            + ")");
    jdbcTemplate.execute(
        "CREATE TABLE IF NOT EXISTS users_roles("
            + "role_id int,"
            + "user_id int,"
            + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,"
            + "FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE"
            + ")");
    jdbcTemplate.update(
        "INSERT IGNORE INTO media (id, url, media_type) VALUES (?, ?, ?)", 1, "url", "IMAGE");
    jdbcTemplate.update(
        "INSERT IGNORE INTO roles (id, name) VALUES (?, ?)",
        2,
        "ROLE_USER"
    );
    user =
        User.builder()
            .id(1L)
            .username("username")
            .email("email@gmail.com")
            .password("HashedPassword")
            .gender(Gender.MALE)
            .media(Media.builder().id(1L).mediaType(MediaType.IMAGE).url("url").build())
            .roles(
                List.of(
                    Role.builder()
                        .id(2L)
                        .name("ROLE_USER")
                        .privileges(List.of(Privilege.builder().id(2L).name("READ").build()))
                        .build()))
            .bio("bio")
            .enable(false)
            .build();
  }

  @Test
  void register() {
    User savedUser = userDao.register(user);

    assertNotNull(savedUser);
    assertNotNull(savedUser.getId());

    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE id = ? AND username = ?",
            Integer.class,
            savedUser.getId(),
            "username");

    assertEquals(1, count);

    Integer roleCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users_roles WHERE user_id = ? AND role_id = 2",
            Integer.class,
            savedUser.getId());

    assertEquals(1, roleCount);
  }

  @Test
  void login() {
    var savedUser = userDao.login("username");

    assertNotNull(savedUser);
    assertEquals(1L, savedUser.getId());
    assertEquals("username", savedUser.getUsername());
    assertEquals("email@gmail.com", savedUser.getEmail());
    assertFalse(savedUser.getEnable());
  }

  @Test
  void findUserById() {
    var foundUser = userDao.findUserById(1L);

    assertNotNull(foundUser);
    assertEquals(1L, foundUser.getId());
    assertEquals("username", foundUser.getUsername());
    assertEquals("email@gmail.com", foundUser.getEmail());
    assertFalse(foundUser.getEnable());
  }

  @Test
  void findUserByUsername() {
    var foundUser = userDao.findUserByUsername("username");

    assertNotNull(foundUser);
    assertEquals(1L, foundUser.getId());
    assertEquals("username", foundUser.getUsername());
    assertEquals("email@gmail.com", foundUser.getEmail());
    assertFalse(foundUser.getEnable());
  }

  @Test
  void findUserByEmail() {
    var foundUser = userDao.findUserByEmail("email@gmail.com");

    assertNotNull(foundUser);
    assertEquals(1L, foundUser.getId());
    assertEquals("username", foundUser.getUsername());
    assertEquals("email@gmail.com", foundUser.getEmail());
    assertFalse(foundUser.getEnable());
  }


}
