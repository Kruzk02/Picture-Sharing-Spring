package com.app.DTO.response;

import com.app.Model.Hashtag;
import java.time.LocalDateTime;
import java.util.List;

public record PinResponse(
    Long id,
    Long userId,
    String description,
    Long mediaId,
    List<Hashtag> tag,
    LocalDateTime createdAt) {}
