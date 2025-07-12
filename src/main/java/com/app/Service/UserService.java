package com.app.Service;

import com.app.DTO.request.LoginUserRequest;
import com.app.DTO.request.RegisterUserRequest;
import com.app.DTO.request.UpdateUserRequest;
import com.app.Model.User;

public interface UserService {
  User register(RegisterUserRequest request);

  User login(LoginUserRequest request);

  User findFullUserByUsername(String username);

  User update(UpdateUserRequest request);

  void verifyAccount(String token);
}
