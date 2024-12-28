package com.app.DTO.response;

import com.app.Model.Gender;

public record UserResponse(Long id,
                           String username,
                           String email,
                           String profilePicture,
                           String password,
                           String bio,
                           Gender gender
) { }
