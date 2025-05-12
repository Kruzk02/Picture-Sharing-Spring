package com.app.DTO.request;

public record LoginUserRequest(
        String username,
        String password
) { }
