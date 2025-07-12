package com.app.DTO.request;

import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public record CreateCommentRequest(
    String content, Long pinId, MultipartFile media, Set<String> tags) {}
