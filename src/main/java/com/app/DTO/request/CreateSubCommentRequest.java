package com.app.DTO.request;

public record CreateSubCommentRequest(
    String content,
    Long commentId
) { }
