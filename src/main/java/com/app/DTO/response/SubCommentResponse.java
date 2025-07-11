package com.app.DTO.response;

import java.time.LocalDateTime;

public record SubCommentResponse(
    Long id,
    String content,
    long mediaId,
    CommentDTO commentDTO,
    UserDTO userDTO,
    LocalDateTime createAt) {}
