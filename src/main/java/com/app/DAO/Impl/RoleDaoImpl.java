package com.app.DAO.Impl;

import com.app.DAO.RoleDao;
import com.app.Model.Privilege;
import com.app.Model.Role;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** Implementation of RoleDao using Spring JDBC Data access. */
@Repository
public class RoleDaoImpl implements RoleDao {

  private final JdbcTemplate jdbcTemplate;

  @Autowired
  public RoleDaoImpl(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Role create(Role role) {
    String sql = "INSERT INTO roles (name) VALUES (?)";
    KeyHolder keyHolder = new GeneratedKeyHolder();

    int rowsAffected =
        jdbcTemplate.update(
            connection -> {
              PreparedStatement ps =
                  connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
              ps.setString(1, role.getName());
              return ps;
            },
            keyHolder);

    if (rowsAffected > 0) {
      role.setId(keyHolder.getKey().longValue());
      return role;
    } else {
      return null;
    }
  }

  @Override
  public Role findByName(String name) {
    try {
      String sql =
          "SELECT r.id AS role_id, r.name AS role_name, p.id AS privilege_id, p.name AS privilege_name "
              + "FROM roles r "
              + "JOIN roles_privileges rp ON rp.role_id = r.id "
              + "JOIN privileges p ON rp.privilege_id = p.id "
              + "WHERE r.name = ?";

      return jdbcTemplate.query(
          sql,
          rs -> {
            Role role = null;
            Set<Privilege> privileges = new HashSet<>();
            while (rs.next()) {
              if (role == null) {
                role = new Role();
                role.setId(rs.getLong("role_id"));
                role.setName(rs.getString("role_name"));
              }
              Privilege privilege = new Privilege();
              privilege.setId(rs.getLong("privilege_id"));
              privilege.setName(rs.getString("privilege_name"));
              privileges.add(privilege);
            }
            if (role != null) {
              role.setPrivileges(privileges);
            }
            return role;
          },
          name);

    } catch (EmptyResultDataAccessException e) {
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
