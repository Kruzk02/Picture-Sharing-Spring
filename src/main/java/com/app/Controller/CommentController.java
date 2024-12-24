package com.app.Controller;

import com.app.DTO.request.CreateCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.DTO.response.CommentResponse;
import com.app.Model.Comment;
import com.app.Model.SortType;
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

import java.util.List;

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

    //TODO: Move findByPinId method to PinController.
//    private List<Comment> findByPinIdHelper(Long pinId, int limit, int offset, SortType sortType) {
//        return switch (sortType) {
//            case NEWEST -> commentService.findNewestByPinId(pinId, limit, offset);
//            case OLDEST -> commentService.findOldestByPinId(pinId, limit, offset);
//            default -> commentService.findByPinId(pinId, limit, offset);
//        };
//    }
//
//    @Operation(summary = "Retrieve comments for a specific pin",
//            description = "Fetch a list of comments associated with a specific pin")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Successfully retrieved comments",
//                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommentResponse.class))),
//            @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
//            @ApiResponse(responseCode = "404", description = "No comments found for the given pin"),
//            @ApiResponse(responseCode = "500", description = "Internal server error")
//    })
//    @GetMapping("/pin/{pinId}/comments")
//    public ResponseEntity<List<CommentResponse>> findByPinId(
//            @Parameter(description = "ID of the pin whose comments are to be retrieved", example = "12", required = true)
//            @PathVariable Long pinId,
//
//            @Parameter(description = "Sorting type for comments: NEWEST, OLDEST, or DEFAULT", example = "NEWEST")
//            @RequestParam(defaultValue = "DEFAULT") SortType sortType,
//
//            @Parameter(description = "Maximum number of comments to retrieve", example = "10")
//            @RequestParam(defaultValue = "10") int limit,
//
//            @Parameter(description = "Offset for pagination, indicating the starting point", example = "0")
//            @RequestParam(defaultValue = "0") int offset
//    ) {
//        if (limit <= 0 || offset < 0) {
//            throw new IllegalArgumentException("Limit must be greater than 0 and offset must be non-negative.");
//        }
//
//        List<Comment> comments = findByPinIdHelper(pinId, limit, offset, sortType);
//        if (comments.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//
//        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(comments.stream()
//            .map(comment -> new CommentResponse(
//                comment.getId(),
//                comment.getContent(),
//                comment.getPinId(),
//                comment.getUserId(),
//                comment.getMediaId()))
//            .toList());
//    }

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
    //TODO: Move deleteByPinId method to PinController.
//    @Operation(summary = "Delete comments by Pin ID")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "204", description = "Comments successfully deleted"),
//            @ApiResponse(responseCode = "404", description = "No comments found for the given Pin ID")
//    })
//    @DeleteMapping("/pin/{pinId}/comments")
//    public ResponseEntity<Void> deleteByPinId(
//            @Parameter(description = "Pin ID of the comments to delete")
//            @PathVariable Long pinId
//    ){
//        commentService.deleteByPinId(pinId);
//        return ResponseEntity.status(HttpStatus.NO_CONTENT)
//                .contentType(MediaType.APPLICATION_JSON)
//                .build();
//    }
}
