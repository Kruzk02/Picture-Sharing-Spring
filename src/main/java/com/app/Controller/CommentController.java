package com.app.Controller;

import com.app.DTO.request.CreateCommentRequest;
import com.app.DTO.response.CreateCommentResponse;
import com.app.DTO.response.PinDTO;
import com.app.DTO.response.UserDTO;
import com.app.Model.Comment;
import com.app.Service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;

    }

    @Operation(description = "create an comment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Create an comment",
                content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Comment.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/create")
    public ResponseEntity<CreateCommentResponse> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Comment to created", required = true,
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Comment.class))
        )
        @RequestBody CreateCommentRequest request
    ){
        Comment comment = commentService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateCommentResponse(
                        comment.getId(),
                        comment.getContent(),
                        new PinDTO(comment.getPin().getId(), comment.getPin().getUserId(), comment.getPin().getDescription()),
                        new UserDTO(comment.getUser().getId(), comment.getUser().getUsername()))
                );
    }

    @Operation(summary = "Delete a comment by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204",description = "Successfully delete an comment")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id of the comment deleted")
            @PathVariable Long id
    ){
        commentService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .contentType(MediaType.APPLICATION_JSON)
                .build();
    }
}
