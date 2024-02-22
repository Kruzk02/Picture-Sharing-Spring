package com.app.DAO.Impl;

import com.app.DAO.RolePrivilegeDao;
import com.app.Model.Privilege;
import com.app.Model.Role;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
public class RolePrivilegeDaoImpl implements RolePrivilegeDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RolePrivilegeDaoImpl(JdbcTemplate jdbcTemplate, RoleDaoImpl roleDao) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addPrivilegeToRole(Role role, Privilege privilege) {
        if(role.getId() != null && privilege.getId() != null){
            String sql = "INSERT INTO roles_privileges(roles_id, privileges_id) VALUES (?, ?)";
            jdbcTemplate.update(sql,role.getId(), privilege.getId());
        }else{
            //after done insert you have to comment addPrivilegeToRole method in DatabaseInitializationServer class.
            log.warn("Please reset a project to insert roles privilege table.");
        }
    }
}
