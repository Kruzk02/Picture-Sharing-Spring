package com.app.DTO.response;

import com.app.Model.Gender;

public record UpdateUserResponse(
        Long id,
        String username,
        String email,
        String profilePicture,
        String bio,
        Gender gender
) { }
