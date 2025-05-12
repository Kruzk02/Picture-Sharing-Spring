package com.app.DTO.request;

import org.springframework.web.multipart.MultipartFile;

public record CreateCommentRequest(
    String content,
    Long pinId,
    MultipartFile media
) { }
