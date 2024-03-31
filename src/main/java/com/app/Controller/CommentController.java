package com.app.Controller;

import com.app.DTO.CommentDTO;
import com.app.Jwt.JwtProvider;
import com.app.Model.Comment;
import com.app.Model.User;
import com.app.Service.CommentService;
import com.app.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;
    private final JwtProvider jwtProvider;

    @Autowired
    public CommentController(CommentService commentService, UserService userService, JwtProvider jwtProvider) {
        this.commentService = commentService;
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/create")
    public ResponseEntity<Comment> create(@RequestBody CommentDTO commentDTO, @RequestHeader("Authorization") String authHeader){
        String token = extractToken(authHeader);

        if(token != null){
            String username = jwtProvider.extractUsername(token);
            User user = userService.findUserByUsername(username);

            commentDTO.setUser(user);

            Comment comment = commentService.save(commentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comment> find(@PathVariable Long id){
        try{
            Comment comment = commentService.findByID(id);
            return ResponseEntity.status(HttpStatus.OK).body(comment);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        try{
            commentService.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).build();
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private String extractToken(String authHeader){
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            return authHeader.substring(7);
        }
        return null;
    }
}
