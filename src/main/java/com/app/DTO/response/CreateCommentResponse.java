package com.app.DTO.response;

public record CreateCommentResponse(
        Long id,
        String content,
        PinDTO pinDTO,
        UserDTO userDTO
) { }
