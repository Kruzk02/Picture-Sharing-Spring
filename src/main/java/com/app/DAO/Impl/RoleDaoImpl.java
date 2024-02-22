package com.app.DAO.Impl;

import com.app.DAO.RoleDao;
import com.app.Model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Repository
public class RoleDaoImpl implements RoleDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RoleDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(Role role) {
        String sql = "INSERT INTO roles (name) VALUES (?) ON DUPLICATE KEY UPDATE name = name";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, role.getName());
            return ps;
        }, keyHolder);

        if (rowsAffected > 0) {
            role.setId(keyHolder.getKey().longValue());
        }
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
                    return role;
                }
                return null;
            }
        });
    }
}
