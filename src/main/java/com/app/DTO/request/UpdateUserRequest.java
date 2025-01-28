package com.app.DTO.request;

import com.app.Model.Gender;
import org.springframework.web.multipart.MultipartFile;

public record UpdateUserRequest(
        String username,
        String email,
        String password,
        String bio,
        Gender gender,
        MultipartFile profilePicture
) { }
