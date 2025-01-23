package com.app.Controller;

import com.app.DTO.request.CreateCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.DTO.response.CommentDTO;
import com.app.DTO.response.CommentResponse;
import com.app.DTO.response.SubCommentResponse;
import com.app.DTO.response.UserDTO;
import com.app.Model.Comment;
import com.app.Model.SortType;
import com.app.Model.SubComment;
import com.app.Service.CommentService;
import com.app.Service.SubCommentService;
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

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@AllArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final SubCommentService subCommentService;

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

    @Operation(summary = "Fetch all sub comments by comment id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetch all sub comments",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SubComment.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}/sub-comments")
    public ResponseEntity<List<SubCommentResponse>> findAllSubCommentById(
            @Parameter(description = "Comment Id of the sub comments to be searched")
            @PathVariable Long id,
            @Parameter(description = "Sorting type for sub comments: NEWEST, OLDEST or DEFAULT")
            @RequestParam(defaultValue = "DEFAULT") SortType sortType,
            @Parameter(description = "Maximum number of sub comments to be retrieved")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Offset for pagination, indicating the starting point")
            @RequestParam(defaultValue = "0") int offset
    ) {
        if (limit <= 0 || offset < 0) {
            throw new IllegalArgumentException("Limit must be greater than 0 and offset must be non-negative.");
        }

        List<SubComment> subComments = findAllByCommentIdHelper(id, limit, offset, sortType);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(subComments.stream().map(subComment ->
                        new SubCommentResponse(
                                subComment.getId(),
                                subComment.getContent(),
                                subComment.getMedia().getId(),
                                new CommentDTO(subComment.getComment().getId(), subComment.getComment().getContent()),
                                new UserDTO(subComment.getUser().getId(), subComment.getUser().getUsername()),
                                subComment.getCreateAt()
                        )).toList());
    }

    private List<SubComment> findAllByCommentIdHelper(long commentId, int limit, int offset, SortType sortType) {
        return switch (sortType) {
            case DEFAULT -> subCommentService.findAllByCommentId(commentId, limit, offset);
            case NEWEST -> subCommentService.findNewestByCommentId(commentId, limit, offset);
            case OLDEST -> subCommentService.findOldestByCommentId(commentId, limit, offset);
        };
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
