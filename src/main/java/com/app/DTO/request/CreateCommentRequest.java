package com.app.DTO.request;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public record CreateCommentRequest(
    String content,
    Long pinId,
    MultipartFile media,
    Set<String> tags
) { }
