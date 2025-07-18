package com.app.DAO;

import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractMySQLTest {

  @Container
  static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8");

  protected JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUpBase() {
    DataSource dataSource = DataSourceBuilder.create()
        .url(mysql.getJdbcUrl())
        .username(mysql.getUsername())
        .password(mysql.getPassword())
        .driverClassName(mysql.getDriverClassName())
        .build();

    jdbcTemplate = new JdbcTemplate(dataSource);

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
  }
}