package com.app.DAO;

import com.app.Model.Role;

public interface RoleDao {

    Role create(Role role);

    Role findByName(String name);
}
