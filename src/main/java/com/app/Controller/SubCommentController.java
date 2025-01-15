package com.app.Controller;

import com.app.DTO.request.CreateSubCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.DTO.response.SubCommentResponse;
import com.app.Model.SortType;
import com.app.Model.SubComment;
import com.app.Service.SubCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subcomment")
@AllArgsConstructor
public class SubCommentController {

    private final SubCommentService subCommentService;

    @Operation(summary = "Fetch all sub comments by comment id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetch all sub comments",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SubComment.class))}),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{commentId}/comment")
    public ResponseEntity<List<SubCommentResponse>> findAllByCommentId(
            @Parameter(description = "Comment Id of the sub comments to be searched")
            @PathVariable Long commentId,
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

        List<SubComment> subComments = findAllByCommentIdHelper(commentId, limit, offset, sortType);
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(subComments.stream().map(subComment ->
                new SubCommentResponse(
                    subComment.getId(),
                    subComment.getContent(),
                    subComment.getMedia().getId(),
                    subComment.getComment().getId(),
                    subComment.getUser().getUsername()
                )).toList());
    }

    private List<SubComment> findAllByCommentIdHelper(long commentId, int limit, int offset, SortType sortType) {
        return switch (sortType) {
            case DEFAULT -> subCommentService.findAllByCommentId(commentId, limit, offset);
            case NEWEST -> subCommentService.findNewestByCommentId(commentId, limit, offset);
            case OLDEST -> subCommentService.findOldestByCommentId(commentId, limit, offset);
        };
    }

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success updated an sub comment",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SubCommentResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Sub comment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SubCommentResponse> update(
            @Parameter(description = "Id of the sub comment to be updated")
            @PathVariable Long id,
            @ModelAttribute UpdatedCommentRequest request) {
        SubComment subComment = subCommentService.update(id, request);
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SubCommentResponse(
                subComment.getId(),
                subComment.getContent(),
                subComment.getMedia().getId(),
                subComment.getComment().getId(),
                subComment.getUser().getUsername()
            ));
    }

    @Operation(summary = "Fetch sub comment by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",  description = "Successfully fetch sub comment",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SubComment.class))}),
        @ApiResponse(responseCode = "404",  description = "Sub comment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SubCommentResponse> findById(
        @Parameter(description = "Id of the sub comment to be searched")
        @PathVariable Long id
    ) {
        SubComment subComment = subCommentService.findById(id);
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SubCommentResponse(
                subComment.getId(),
                subComment.getContent(),
                subComment.getMedia().getId(),
                subComment.getComment().getId(),
                subComment.getUser().getUsername()
            ));
    }

    @Operation(summary = "Create new sub comment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully create new sub comment",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SubComment.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid Input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<SubCommentResponse> save(
        @RequestBody(
            description = "SubComment to created", required = true,
            content = @Content(mediaType = "application/json")
        )
        @ModelAttribute CreateSubCommentRequest request
    ) {
        SubComment subComment = subCommentService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SubCommentResponse(
                subComment.getId(),
                subComment.getContent(),
                subComment.getMedia().getId(),
                subComment.getComment().getId(),
                subComment.getUser().getUsername()
            ));
    }

    @Operation(summary = "Delete sub comment by it ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully delete sub comment",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Sub comment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "Id of the sub comment to be searched")
            @PathVariable Long id
    ) {
        subCommentService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
