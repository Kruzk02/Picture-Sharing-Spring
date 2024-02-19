package com.app.DAO;

import com.app.DTO.LoginDTO;
import com.app.DTO.RegisterDTO;
import com.app.Model.User;

public interface UserDao {

    User register(User user);
    User login(User user);
    User findUserById(Long id);
    User findUserByUsername(String username);
}
