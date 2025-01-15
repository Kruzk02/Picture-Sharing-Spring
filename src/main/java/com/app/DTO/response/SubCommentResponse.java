package com.app.DTO.response;

public record SubCommentResponse(
        Long subCommentId,
        String content,
        long mediaId,
        long commentId,
        String username
) { }

