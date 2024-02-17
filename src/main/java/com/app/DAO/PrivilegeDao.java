package com.app.DAO;

import com.app.Model.Privilege;

public interface PrivilegeDao {

    void create(Privilege privilege);

    Privilege findByName(String name);
}
