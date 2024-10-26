package com.app.Controller;

import com.app.DTO.CommentDTO;
import com.app.Model.Comment;
import com.app.Model.User;
import com.app.Service.CommentService;
import com.app.Service.UserService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @Autowired
    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    @Operation(description = "create an comment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Create an comment",
                content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Comment.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/create")
    public ResponseEntity<Comment> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Comment to created", required = true,
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Comment.class))
        )
        @RequestBody CommentDTO commentDTO
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(authentication.getName());

        commentDTO.setUser(user);

        Comment comment = commentService.save(commentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(comment);
    }

    @Operation(summary = "Delete a comment by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204",description = "Successfully delete an comment")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "Id of the comment deleted")
            @PathVariable Long id
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(authentication.getName());
        commentService.deleteIfUserMatches(user,id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).contentType(MediaType.APPLICATION_JSON).build();
    }
}
