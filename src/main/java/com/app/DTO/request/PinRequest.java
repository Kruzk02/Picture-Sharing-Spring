package com.app.DTO.request;

import org.springframework.web.multipart.MultipartFile;

public record PinRequest(String description, MultipartFile file) { }
