package com.app.DTO.request;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public record UpdatedCommentRequest(String content, MultipartFile media, Set<String> tags) {
}
