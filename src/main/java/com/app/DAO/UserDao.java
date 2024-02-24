package com.app.DAO;

import com.app.DTO.LoginDTO;
import com.app.DTO.RegisterDTO;
import com.app.Model.Role;
import com.app.Model.User;

public interface UserDao {

    User register(User user);
    User login(String username);
    User findUserById(Long id);
    User findUserByUsername(String username);
    User findUserByEmail(String email);
}
