package com.app.DTO.request;

import org.springframework.web.multipart.MultipartFile;

public record UpdatedMediaRequest(MultipartFile file) {}
