package com.app.DTO.response;

import java.time.LocalDateTime;

public record PinResponse(
        Long id,
        Long userId,
        String description,
        Long mediaId,
        LocalDateTime createdAt
) { }
