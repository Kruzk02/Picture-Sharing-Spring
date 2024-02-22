package com.app.Service;

import com.app.DAO.Impl.PrivilegeDaoImpl;
import com.app.DAO.Impl.RoleDaoImpl;
import com.app.DAO.Impl.RolePrivilegeDaoImpl;
import com.app.Model.Privilege;
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
import java.util.Arrays;
import java.util.List;

@Service
@Log4j2
public class DatabaseInitializationService {

    private final JdbcTemplate jdbcTemplate;
    private final RoleDaoImpl roleDao;
    private final PrivilegeDaoImpl privilegeDao;
    private final RolePrivilegeDaoImpl rolePrivilegeDao;

    @Autowired
    public DatabaseInitializationService(JdbcTemplate jdbcTemplate, RoleDaoImpl roleDao, PrivilegeDaoImpl privilegeDao, RolePrivilegeDaoImpl rolePrivilegeDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.roleDao = roleDao;
        this.privilegeDao = privilegeDao;
        this.rolePrivilegeDao = rolePrivilegeDao;
    }

    @PostConstruct
    public void initializeDatabase() {
        try {
            executeSqlScript("user.sql");
            executeSqlScript("board.sql");
            executeSqlScript("pin.sql");
            executeSqlScript("roles.sql");
            executeSqlScript("privileges.sql");
            executeSqlScript("roles_privileges.sql");
            executeSqlScript("users_roles.sql");

            Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
            Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");

            List<Privilege> adminPrivilege = Arrays.asList(readPrivilege,writePrivilege);
            Role adminRole = createRoleIfNotFound("ROLE_ADMIN",adminPrivilege);
            Role userRole = createRoleIfNotFound("ROLE_USER",Arrays.asList(readPrivilege));

//            rolePrivilegeDao.addPrivilegeToRole(adminRole,readPrivilege);
//            rolePrivilegeDao.addPrivilegeToRole(adminRole,writePrivilege);
//            rolePrivilegeDao.addPrivilegeToRole(userRole,readPrivilege);

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

    private Privilege createPrivilegeIfNotFound(String name){
        log.info("Checking if privilege exist with a name: {}",name);

        Privilege privilege = privilegeDao.findByName(name);
        if(privilege == null){
            log.info("Privilege with name {} not found. Creating new privilege.", name);

            privilege = new Privilege(name);
            privilegeDao.create(privilege);

            log.info("Privilege created: {}", privilege);
        }else{
            log.info("Privilege with name {} already exist",name);
        }
        return privilege;
    }

    private Role createRoleIfNotFound(String name, List<Privilege> privileges){
        log.info("Checking if role exist with a name: {}",name);
        Role role = roleDao.findByName(name);
        if(role == null){
            log.info("Role with name {} not found. Create new Role",name);

            role = new Role(name);
            role.setPrivileges(privileges);
            roleDao.create(role);

            log.info("Role created: {}",role);
        }else{
            log.info("Role with name {} already exist",name);
        }
        return role;
    }
}