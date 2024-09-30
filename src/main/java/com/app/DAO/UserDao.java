package com.app.DAO;

import com.app.Model.User;

/**
 * Interface for managing User data access operations.
 */
public interface UserDao {

    /**
     * Registers a new user.
     *
     * @param user the user object to register.
     * @return The registered user object
     */
    User register(User user);

    /**
     * Logs in a user based on the username
     *
     * @param username The username of the user to log in.
     * @return the logged-in user object.
     */
    User login(String username);

    /**
     * Finds a user by their id
     *
     * @param id The id of the user to find.
     * @return the user object found, or null if not found.
     */
    User findUserById(Long id);

    /**
     * Finds a user by their username.
     *
     * @param username The username of the user to find
     * @return The user object found, or null if not found.
     */
    User findUserByUsername(String username);

    /**
     * Finds a user by their email.
     *
     * @param email the email address of the user to find
     * @return The user object found, or null if not found
     */
    User findUserByEmail(String email);

    User findPasswordNRoleByUsername(String username);
    User update(User user);
}
