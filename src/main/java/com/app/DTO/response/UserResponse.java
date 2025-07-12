package com.app.DTO.response;

import com.app.Model.Gender;

public record UserResponse(
    Long id, String username, String email, long mediaId, String bio, Gender gender) {}
