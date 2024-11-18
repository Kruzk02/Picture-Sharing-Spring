package com.app.DTO.response;

import java.time.LocalDateTime;

public record CreateSubCommentResponse(
        Long subCommentId,
        String content,
        CommentDTO comment,
        UserDTO user,
        LocalDateTime localDateTime
) { }

