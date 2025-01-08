package com.app.DTO.response;

import java.sql.Timestamp;

public record PinResponse(
        Long id,
        Long userId,
        String description,
        Long mediaId,
        Timestamp createdAt
) { }
