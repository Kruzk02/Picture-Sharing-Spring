package com.app.DAO.Impl;

import com.app.DAO.RoleDao;
import com.app.Model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RoleDaoImpl implements RoleDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RoleDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(Role role) {
        jdbcTemplate.update(
                "INSERT INTO roles (id,name) VALUES (?,?)",
                role.getId(),
                role.getName()
        );
    }
}
