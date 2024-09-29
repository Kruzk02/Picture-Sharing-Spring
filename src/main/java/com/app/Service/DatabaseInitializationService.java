package com.app.Service;

import com.app.DAO.PrivilegeDao;
import com.app.DAO.RoleDao;
import com.app.Model.Privilege;
import com.app.Model.Role;
import com.app.Model.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Service
@Log4j2
public class DatabaseInitializationService implements ApplicationListener<ContextRefreshedEvent> {

    private final JdbcTemplate jdbcTemplate;
    private final RoleDao roleDao;
    private final PrivilegeDao privilegeDao;
    private final PasswordEncoder passwordEncoder;
    private boolean alreadySetup = false;

    @Autowired
    public DatabaseInitializationService(JdbcTemplate jdbcTemplate, RoleDao roleDao, PrivilegeDao privilegeDao, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.roleDao = roleDao;
        this.privilegeDao = privilegeDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) return;

        executeSqlScript("user.sql");
        executeSqlScript("board.sql");
        executeSqlScript("pin.sql");
        executeSqlScript("board_pin.sql");
        executeSqlScript("roles.sql");
        executeSqlScript("privileges.sql");
        executeSqlScript("roles_privileges.sql");
        executeSqlScript("users_roles.sql");
        executeSqlScript("comment.sql");
        executeSqlScript("sub_comment.sql");

        createPrivilegeIfNotFound("READ_PRIVILEGE");
        createPrivilegeIfNotFound("WRITE_PRIVILEGE");

        createRoleIfNotFound("ROLE_ADMIN");
        createRoleIfNotFound("ROLE_USER");

        User user = User.builder()
                .email("phucnguyen@gmail.com")
                .username("phucnguyen")
                .password(passwordEncoder.encode("123123"))
                .build();
        createAdminAccount(user);

        alreadySetup = true;
    }

    private void executeSqlScript(String fileName) {
        Resource resource = new ClassPathResource(fileName);
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            String sqlScript = FileCopyUtils.copyToString(reader);
            jdbcTemplate.execute(sqlScript);
            log.info("Successfully executed SQL script from file: {}", fileName);
        } catch (Exception e) {
            log.error("Error executing SQL script from file {}: {}", fileName, e.getMessage());
        }
    }

    private void createAdminAccount(User user) {

        String userSql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            return ps;
        }, keyHolder);

        if (rowsAffected > 0) {
            user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

            String userRoleSql = "INSERT INTO users_roles (user_id, role_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(userRoleSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, user.getId());
                    ps.setInt(2, 1);
                }

                @Override
                public int getBatchSize() {
                    return 1;
                }
            });

        }
    }
    private void createRoleIfNotFound(String name){
        log.info("Checking if role exist with a name: {}",name);
        Role role = roleDao.findByName(name);
        Optional<Role> roleOptional = Optional.ofNullable(role);
        if(roleOptional.isEmpty()){
            log.info("Role with name {} not found. Create new Role",name);
            role = new Role(name);
            roleDao.create(role);

            log.info("Role created: {}",role);
        }
    }

    private void createPrivilegeIfNotFound(String name){
        log.info("Checking if privilege exist with a name: {}",name);
        Privilege privilege = privilegeDao.findByName(name);
        Optional<Privilege> privilegeOptional = Optional.ofNullable(privilege);
        if (privilegeOptional.isEmpty()) {
            log.info("Privilege with name {} not found. Create new Privilege",name);
            privilege = new Privilege(name);
            privilegeDao.create(privilege);
            log.info("Privilege create: {}",privilege);
        }
    }

}