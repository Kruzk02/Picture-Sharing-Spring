package com.app.DTO.request;

import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public record UpdatedCommentRequest(String content, MultipartFile media, Set<String> tags) {}
