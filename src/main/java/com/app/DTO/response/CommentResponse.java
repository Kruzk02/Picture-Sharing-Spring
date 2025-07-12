package com.app.DTO.response;

import com.app.Model.Hashtag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Details of a single comment")
public record CommentResponse(
    @Schema(description = "Id of the comment", example = "123") Long id,
    @Schema(description = "Content of the comment", example = "HELLO WORLD") String content,
    @Schema(description = "Id of the pin associated with comment", example = "123") long pinId,
    @Schema(description = "Id of the user associated with comment", example = "123") long userId,
    @Schema(description = "Id of the media associated with comment", example = "123") long mediaId,
    LocalDateTime created_at,
    List<Hashtag> tag) {}
