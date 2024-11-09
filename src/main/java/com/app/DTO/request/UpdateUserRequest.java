package com.app.DTO.request;

import com.app.Model.Gender;
import jakarta.annotation.Nullable;

public record UpdateUserRequest(
        @Nullable String username,
        @Nullable String email,
        @Nullable String password,
        @Nullable String bio,
        @Nullable Gender gender
) { }
