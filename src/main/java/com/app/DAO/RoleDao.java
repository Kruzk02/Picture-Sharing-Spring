package com.app.DAO;

import com.app.Model.Role;

/** Interface for managing Role data access operations. */
public interface RoleDao {

  /**
   * Create a new role.
   *
   * @param role the role object to create.
   * @return the created role object.
   */
  Role create(Role role);

  /**
   * Finds a role by role name.
   *
   * @param name the name of the role to find.
   * @return the role found, or null if not found.
   */
  Role findByName(String name);
}
