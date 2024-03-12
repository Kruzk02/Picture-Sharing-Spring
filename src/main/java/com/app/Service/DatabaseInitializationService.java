package com.app.Service;

import com.app.DAO.Impl.RoleDaoImpl;
import com.app.Model.Role;
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

    private final JdbcTemplate jdbcTemplate;
    private final RoleDaoImpl roleDao;

    @Autowired
    public DatabaseInitializationService(JdbcTemplate jdbcTemplate, RoleDaoImpl roleDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.roleDao = roleDao;
    }

    @PostConstruct
    public void initializeDatabase() {
        try {
            executeSqlScript("user.sql");
            executeSqlScript("board.sql");
            executeSqlScript("pin.sql");
            executeSqlScript("board_pin.sql");
            executeSqlScript("roles.sql");
            executeSqlScript("users_roles.sql");

            Role adminRole = createRoleIfNotFound("ADMIN");
            Role userRole = createRoleIfNotFound("USER");

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

    private Role createRoleIfNotFound(String name){
        log.info("Checking if role exist with a name: {}",name);
        Role role = roleDao.findByName(name);
        if(role == null){
            log.info("Role with name {} not found. Create new Role",name);

            role = new Role(name);
            roleDao.create(role);

            log.info("Role created: {}",role);
        }else{
            log.info("Role with name {} already exist",name);
        }
        return role;
    }
}