package com.app.DTO.request;

import org.springframework.web.multipart.MultipartFile;

public record CreateSubCommentRequest(
    String content,
    MultipartFile file,
    Long commentId
) { }
