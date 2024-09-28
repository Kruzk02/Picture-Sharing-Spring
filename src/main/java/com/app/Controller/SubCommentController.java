package com.app.Controller;

import com.app.DTO.SubCommentDTO;
import com.app.Model.Comment;
import com.app.Model.SubComment;
import com.app.Model.User;
import com.app.Service.CommentService;
import com.app.Service.SubCommentService;
import com.app.Service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final CommentService commentService;

    @GetMapping("/comment/{commentId}")
    public ResponseEntity<List<SubComment>> findAllByCommentId(@PathVariable Long commentId) {
        List<SubComment> subComments = subCommentService.findAllByCommentId(commentId);
        return ResponseEntity.ok(subComments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubComment> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(subCommentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<SubComment> save(@RequestBody SubCommentDTO subCommentDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(authentication.getName());

        subCommentDTO.setUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(subCommentService.save(subCommentDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBySubCommentId(@PathVariable Long subCommentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(authentication.getName());
        subCommentService.deleteIfUserMatches(user,subCommentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
