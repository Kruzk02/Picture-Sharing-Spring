package com.app.DTO.response;

import java.sql.Timestamp;

public record SubCommentResponse(
        Long id,
        String content,
        long mediaId,
        CommentDTO commentDTO,
        UserDTO userDTO,
        Timestamp createAt
) { }

