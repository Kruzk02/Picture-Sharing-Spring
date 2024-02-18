package com.app.DAO;

import com.app.Model.Privilege;
import com.app.Model.Role;

public interface RolePrivilegeDao {
    void addPrivilegeToRole(Long id, Privilege privilege);
}
