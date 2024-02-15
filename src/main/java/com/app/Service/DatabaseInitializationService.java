package com.app.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
@Log4j2
public class DatabaseInitializationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initializeDatabase() {
        try {

            executeSqlScript("user.sql");
            executeSqlScript("board.sql");
            executeSqlScript("pin.sql");
        } catch (Exception e) {
            log.error("Error initializing database: {}", e.getMessage());
        }
    }

    private void executeSqlScript(String fileName) throws IOException {
        Resource resource = new ClassPathResource(fileName);
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            String sqlScript = FileCopyUtils.copyToString(reader);
            jdbcTemplate.execute(sqlScript);
            log.info("Successfully executed SQL script from file: {}", fileName);
        } catch (Exception e) {
            log.error("Error executing SQL script from file {}: {}", fileName, e.getMessage());
        }
    }
}