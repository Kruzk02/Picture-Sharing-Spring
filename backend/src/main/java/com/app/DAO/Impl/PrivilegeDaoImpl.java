package com.app.DAO.Impl;

import com.app.DAO.PrivilegeDao;
import com.app.Model.Privilege;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Objects;

@Repository
@AllArgsConstructor
public class PrivilegeDaoImpl implements PrivilegeDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Privilege create(Privilege privilege) {
        String sql = "INSERT INTO privileges (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int row = jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1,privilege.getName());
            return ps;
        },keyHolder);

        if (row > 0) {
            privilege.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
            return privilege;
        }else {
            return null;
        }
    }

    @Override
    public Privilege findByName(String name) {
        try {
            String sql = "SELECT * FROM privileges WHERE name = ?";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Privilege privilege = new Privilege();
                privilege.setId(rs.getLong("id"));
                privilege.setName(rs.getString("name"));
                return privilege;
            }, name);
        }catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
