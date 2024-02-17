package com.app.DAO.Impl;

import com.app.DAO.PrivilegeDao;
import com.app.Model.Privilege;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class PrivilegeDaoImpl implements PrivilegeDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PrivilegeDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(Privilege privilege) {
        jdbcTemplate.update("INSERT INTO privileges (id,name) VALUES (?,?)" +
                        "ON DUPLICATE KEY UPDATE name = name",
                privilege.getId(),
                privilege.getName()
        );
    }

    @Override
    public Privilege findByName(String name) {
        String sql = "SELECT * FROM privileges WHERE name = ?";
        return jdbcTemplate.query(sql, new Object[]{name}, new ResultSetExtractor<Privilege>() {
            @Override
            public Privilege extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (rs.next()) {
                    Privilege privilege = new Privilege();
                    privilege.setId(rs.getLong("id"));
                    privilege.setName(rs.getString("name"));
                    return privilege;
                }
                return null;
            }
        });
    }
}
