package com.app.DAO;

import com.app.Model.Privilege;

public interface PrivilegeDao {

  Privilege create(Privilege privilege);

  Privilege findByName(String name);
}
