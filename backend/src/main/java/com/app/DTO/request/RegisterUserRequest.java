package com.app.DTO.request;

import com.app.Model.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record RegisterUserRequest(
        @NotNull String username,
        @Email String email,
        @NotNull String password
) {}
