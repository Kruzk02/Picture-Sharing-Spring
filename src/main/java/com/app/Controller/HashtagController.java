package com.app.Controller;

import com.app.DTO.response.CommentResponse;
import com.app.DTO.response.PinResponse;
import com.app.Model.Comment;
import com.app.Model.Pin;
import com.app.Service.CommentService;
import com.app.Service.PinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/hashtag")
@AllArgsConstructor
public class HashtagController {

    private final CommentService commentService;
    private final PinService pinService;


    @Operation(summary = "Get all Pins")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get all pins",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Pin.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{tag}/pins")
    public ResponseEntity<List<PinResponse>> getAllPinsByTag(
            @Parameter(description = "tag of the pin", required = true)
            @PathVariable String tag,
            @Parameter(description = "Maximum number of pins to be retrieved")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Offset for pagination, indicating the starting point")
            @RequestParam(defaultValue = "0") int offset
    ) {
        if (limit <= 0 || offset < 0) {
            throw new IllegalArgumentException("Limit must be greater than 0 and offset must be non-negative.");
        }

        List<Pin> pins = pinService.getAllPinsByHashtag(tag, limit, offset);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(pins.stream().map(pin ->
                        new PinResponse(
                                pin.getId(),
                                pin.getUserId(),
                                pin.getDescription(),
                                pin.getMediaId(),
                                new ArrayList<>(),
                                pin.getCreatedAt()
                        )).toList()
                );
    }

    @Operation(summary = "Get all Pins")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get all pins",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Pin.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{tag}/comments")
    public ResponseEntity<List<CommentResponse>> getAllCommentByTag(
            @Parameter(description = "tag of the comments", required = true)
            @PathVariable String tag,
            @Parameter(description = "Maximum number of comments to be retrieved")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Offset for pagination, indicating the starting point")
            @RequestParam(defaultValue = "0") int offset
    ) {
        if (limit <= 0 || offset < 0) {
            throw new IllegalArgumentException("Limit must be greater than 0 and offset must be non-negative.");
        }

        List<Comment> comments = commentService.findByHashtag(tag, limit, offset);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(comments.stream().map(comment ->
                        new CommentResponse(
                                comment.getId(),
                                comment.getContent(),
                                comment.getPinId(),
                                comment.getUserId(),
                                comment.getMediaId(),
                                comment.getCreated_at(),
                                new ArrayList<>()
                        )).toList()
                );
    }
}
