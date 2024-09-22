package com.app.Controller;

import com.app.DTO.BoardDTO;
import com.app.Model.Board;
import com.app.Model.User;
import com.app.Service.BoardService;
import com.app.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;
    private final UserService userService;

    @Autowired
    public BoardController(BoardService boardService, UserService userService) {
        this.boardService = boardService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody BoardDTO boardDTO){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(authentication.getName());

        boardDTO.setUser(user);

        Board board = boardService.save(boardDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(board);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        Board board = boardService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(board);
    }

    @GetMapping
    public ResponseEntity<List<Board>> findAllByUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(authentication.getName());

        List<Board> boards = boardService.findAllByUserId(user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(boards);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(authentication.getName());

        boardService.deleteById(user,id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
