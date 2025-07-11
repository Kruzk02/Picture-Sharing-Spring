package com.app.Controller;

import com.app.DTO.request.CreateSubCommentRequest;
import com.app.DTO.request.UpdatedCommentRequest;
import com.app.DTO.response.CommentDTO;
import com.app.DTO.response.SubCommentResponse;
import com.app.DTO.response.UserDTO;
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

@RestController
@RequestMapping("/api/sub-comment")
@AllArgsConstructor
public class SubCommentController {

  private final SubCommentService subCommentService;

  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Success updated an sub comment",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = SubCommentResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Sub comment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}")
  public ResponseEntity<SubCommentResponse> update(
      @Parameter(description = "Id of the sub comment to be updated") @PathVariable Long id,
      @ModelAttribute UpdatedCommentRequest request) {
    SubComment subComment = subCommentService.update(id, request);
    return ResponseEntity.status(HttpStatus.OK)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            new SubCommentResponse(
                subComment.getId(),
                subComment.getContent(),
                subComment.getMedia().getId(),
                new CommentDTO(
                    subComment.getComment().getId(), subComment.getComment().getContent()),
                new UserDTO(subComment.getUser().getId(), subComment.getUser().getUsername()),
                subComment.getCreateAt()));
  }

  @Operation(summary = "Fetch sub comment by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully fetch sub comment",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = SubComment.class))
            }),
        @ApiResponse(responseCode = "404", description = "Sub comment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<SubCommentResponse> findById(
      @Parameter(description = "Id of the sub comment to be searched") @PathVariable Long id) {
    SubComment subComment = subCommentService.findById(id);
    return ResponseEntity.status(HttpStatus.OK)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            new SubCommentResponse(
                subComment.getId(),
                subComment.getContent(),
                subComment.getMedia().getId(),
                new CommentDTO(
                    subComment.getComment().getId(), subComment.getComment().getContent()),
                new UserDTO(subComment.getUser().getId(), subComment.getUser().getUsername()),
                subComment.getCreateAt()));
  }

  @Operation(summary = "Create new sub comment")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Successfully create new sub comment",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = SubComment.class))
            }),
        @ApiResponse(responseCode = "400", description = "Invalid Input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<SubCommentResponse> save(
      @RequestBody(
              description = "SubComment to created",
              required = true,
              content = @Content(mediaType = "application/json"))
          @ModelAttribute
          CreateSubCommentRequest request) {
    SubComment subComment = subCommentService.save(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            new SubCommentResponse(
                subComment.getId(),
                subComment.getContent(),
                subComment.getMedia().getId(),
                new CommentDTO(
                    subComment.getComment().getId(), subComment.getComment().getContent()),
                new UserDTO(subComment.getUser().getId(), subComment.getUser().getUsername()),
                subComment.getCreateAt()));
  }

  @Operation(summary = "Delete sub comment by it ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Successfully delete sub comment",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Sub comment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(
      @Parameter(description = "Id of the sub comment to be searched") @PathVariable Long id) {
    subCommentService.deleteById(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
