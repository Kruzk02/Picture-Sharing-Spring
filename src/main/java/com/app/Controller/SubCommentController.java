package com.app.Controller;

import com.app.DTO.request.CreateSubCommentRequest;
import com.app.DTO.response.CommentDTO;
import com.app.DTO.response.CreateSubCommentResponse;
import com.app.DTO.response.UserDTO;
import com.app.Model.SubComment;
import com.app.Model.User;
import com.app.Service.SubCommentService;
import com.app.Service.UserService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subcomments")
@AllArgsConstructor
public class SubCommentController {

    private final SubCommentService subCommentService;
    private final UserService userService;

    @Operation(summary = "Fetch all sub comments by comment id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetch all sub comments",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SubComment.class))}),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/comment/{commentId}")
    public ResponseEntity<List<SubComment>> findAllByCommentId(
            @Parameter(description = "Comment Id of the sub comments to be searched")
            @PathVariable Long commentId
    ) {
        List<SubComment> subComments = subCommentService.findAllByCommentId(commentId);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(subComments);
    }

    @Operation(summary = "Fetch sub comment by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",  description = "Successfully fetch sub comment",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SubComment.class))}),
        @ApiResponse(responseCode = "404",  description = "Sub comment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SubComment> findById(
        @Parameter(description = "Id of the sub comment to be searched")
        @PathVariable Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(subCommentService.findById(id));
    }

    @Operation(summary = "Create new sub comment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully create new sub comment",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = SubComment.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid Input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<CreateSubCommentResponse> save(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "SubComment to created", required = true,
            content = @Content(mediaType = "application/json")
        )
        @RequestBody CreateSubCommentRequest request
    ) {
        SubComment subComment = subCommentService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateSubCommentResponse(
                        subComment.getId(),
                        subComment.getContent(),
                        new CommentDTO(subComment.getComment().getId(), subComment.getComment().getContent()),
                        new UserDTO(subComment.getUser().getId(), subComment.getUser().getUsername()),
                        subComment.getTimestamp().toLocalDateTime()
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
    public ResponseEntity<Void> deleteBySubCommentId(
            @Parameter(description = "Id of the sub comment to be searched")
            @PathVariable Long subCommentId
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(authentication.getName());
        subCommentService.deleteIfUserMatches(user,subCommentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
