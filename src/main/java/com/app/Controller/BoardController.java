package com.app.Controller;

import com.app.DTO.BoardDTO;
import com.app.Jwt.JwtProvider;
import com.app.Model.Board;
import com.app.Model.User;
import com.app.Service.BoardService;
import com.app.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;
    private final UserService userService;
    private final JwtProvider jwtProvider;

    @Autowired
    public BoardController(BoardService boardService, UserService userService, JwtProvider jwtProvider) {
        this.boardService = boardService;
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody BoardDTO boardDTO,@RequestHeader("Authorization") String authHeader){
        String token = extractToken(authHeader);

        if(token != null){
            String username = jwtProvider.extractUsername(token);
            User user = userService.findUserByUsername(username);

            boardDTO.setUser(user);

            Board board = boardService.save(boardDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(board);
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Authorization header");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        Board board = boardService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(board);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id){
        boardService.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
