package com.app.DAO.Impl;

import com.app.DAO.RoleDao;
import com.app.Model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

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
                "INSERT INTO roles (id,name) VALUES (?,?)" +
                        "ON DUPLICATE KEY UPDATE name = name",
                role.getId(),
                role.getName()
        );
    }

    @Override
    public Role findByName(String name) {
        String sql = "SELECT * FROM roles WHERE name =?";
        return jdbcTemplate.query(sql,new Object[]{name}, new ResultSetExtractor<Role>() {
            @Override
            public Role extractData(ResultSet rs) throws SQLException, DataAccessException {
                if(rs.next()){
                    Role role = new Role();
                    role.setId(rs.getLong("id"));
                    role.setName(rs.getString("name"));
                }
                return null;
            }
        });
    }
}
