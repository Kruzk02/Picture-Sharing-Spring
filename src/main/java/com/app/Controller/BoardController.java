package com.app.Controller;

import com.app.DTO.BoardDTO;
import com.app.Model.Board;
import com.app.Model.User;
import com.app.Service.BoardService;
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

    @Operation(summary = "Create new board")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully create new board",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Board.class))),
        @ApiResponse(responseCode = "404", description = "Pin not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/create")
    public ResponseEntity<Board> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Board data", required = true,
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BoardDTO.class))
        )
        @RequestBody BoardDTO boardDTO
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(authentication.getName());

        boardDTO.setUser(user);

        Board board = boardService.save(boardDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(board);
    }

    @Operation(summary = "Fetch board by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetch board",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Board.class))),
        @ApiResponse(responseCode = "404", description = "Board not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Board> findById(
        @Parameter(description = "Id of the board to be search", required = true)
        @PathVariable Long id
    ){
        Board board = boardService.findById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(board);
    }

    @Operation(summary = "Fetch all board by user id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetch all board by user id",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Board.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<Board>> findAllByUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(authentication.getName());

        List<Board> boards = boardService.findAllByUserId(user.getId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(boards);
    }

    @Operation(summary = "Delete a board by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204",description = "Successfully delete an comment")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(
        @Parameter(description = "Id of the comment deleted")
        @PathVariable Long id
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(authentication.getName());

        boardService.deleteIfUserMatches(user,id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
