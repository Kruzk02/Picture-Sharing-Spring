package com.app.DTO.response;

import com.app.Model.Gender;

public record RegisterUserResponse(
        Long id,
        String username,
        String email,
        String profilePicture,
        Gender gender
) { }
