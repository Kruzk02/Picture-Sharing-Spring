package com.app.DTO.request;

public record CreateCommentRequest(
    String content,
    Long pinId
) { }
