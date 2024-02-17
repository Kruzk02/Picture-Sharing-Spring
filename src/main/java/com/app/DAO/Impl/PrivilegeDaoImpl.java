package com.app.DAO.Impl;

import com.app.DAO.PrivilegeDao;
import com.app.Model.Privilege;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PrivilegeDaoImpl implements PrivilegeDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PrivilegeDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(Privilege privilege) {
        jdbcTemplate.update("INSERT INTO privileges (id,name) VALUES (?,?)",
                privilege.getId(),
                privilege.getName()
        );
    }
}
