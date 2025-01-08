package com.app.Controller;

import com.app.DTO.request.CreateCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.DTO.response.CommentResponse;
import com.app.Model.Comment;
import com.app.Service.CommentService;
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

@RestController
@RequestMapping("/api/comment")
@AllArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(description = "Get comment details by its ID ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment found",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Comment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> findById(@PathVariable Long id, @RequestParam(defaultValue = "basic") String view) {
        Comment comment;

        if ("details".equalsIgnoreCase(view)) {
            comment = commentService.findDetailsById(id);
        } else {
            comment = commentService.findBasicById(id);
        }

        if (comment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).build();
        }

        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getPinId(),
                comment.getUserId(),
                comment.getMediaId()
            ));
    }

    @Operation(description = "create an comment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Create an comment",
                content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/")
    public ResponseEntity<CommentResponse> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Comment to created", required = true,
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Comment.class))
        )
        @ModelAttribute CreateCommentRequest request
    ){
        Comment comment = commentService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getPinId(),
                comment.getUserId(),
                comment.getMediaId()
            ));
    }

    @Operation(description = "Update an comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success update an comment",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> update(@PathVariable Long id, @ModelAttribute UpdatedCommentRequest request) {
        Comment comment = commentService.update(id, request);
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getPinId(),
                comment.getUserId(),
                comment.getMediaId()
            ));
    }

    @Operation(summary = "Delete a comment by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id of the comment deleted")
            @PathVariable Long id
    ){
        commentService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .contentType(MediaType.APPLICATION_JSON)
                .build();
    }
}
