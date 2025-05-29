package com.app.DTO.request;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public record PinRequest(
        String description,
        MultipartFile file,
        Set<String> hashtags
) { }
