package com.app.Controller;

import com.app.DTO.request.BoardRequest;
import com.app.DTO.request.BoardUpdateRequest;
import com.app.DTO.response.BoardResponse;
import com.app.DTO.response.PinDTO;
import com.app.DTO.response.UserDTO;
import com.app.Model.Board;
import com.app.Service.BoardService;
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

import java.util.Collections;

@RestController
@AllArgsConstructor
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;

    @Operation(summary = "Create new board")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully create new board",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Board.class))),
        @ApiResponse(responseCode = "404", description = "Pin not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/create")
    public ResponseEntity<BoardResponse> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Board data", required = true,
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BoardRequest.class))
        )
        @RequestBody BoardRequest boardRequest
    ){
        Board board = boardService.save(boardRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BoardResponse(board.getId(), board.getName(),
                        new UserDTO(board.getUser().getId(), board.getUser().getUsername()),
                        !board.getPins().isEmpty() ?
                                board.getPins().stream().map(pin -> new PinDTO(pin.getId(), pin.getUserId(), pin.getMediaId())).toList() :
                                Collections.emptyList()
                        )
                );
    }

    @PostMapping("/{boardId}/pin/{pinId}")
    public ResponseEntity<BoardResponse> addPinToBoard(
        @PathVariable Long boardId,
        @PathVariable Long pinId
    ) {
        Board board = boardService.addPinToBoard(pinId, boardId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BoardResponse(board.getId(), board.getName(),
                                new UserDTO(board.getUser().getId(), board.getUser().getUsername()),
                                !board.getPins().isEmpty() ?
                                        board.getPins().stream().map(pin -> new PinDTO(pin.getId(), pin.getUserId(), pin.getMediaId())).toList() :
                                        Collections.emptyList()
                        )
                );
    }

    @DeleteMapping("/{boardId}/pin/{pinId}")
    public ResponseEntity<BoardResponse> deletePinFromBoard(
            @PathVariable Long boardId,
            @PathVariable Long pinId
    ) {
        Board board = boardService.deletePinFromBoard(pinId, boardId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BoardResponse(board.getId(), board.getName(),
                                new UserDTO(board.getUser().getId(), board.getUser().getUsername()),
                                !board.getPins().isEmpty() ?
                                        board.getPins().stream().map(pin -> new PinDTO(pin.getId(), pin.getUserId(), pin.getMediaId())).toList() :
                                        Collections.emptyList()
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardResponse> update(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Board data", required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BoardUpdateRequest.class))
            )
            @RequestBody BoardUpdateRequest request,
            @PathVariable Long id
    ){
        Board board = boardService.update(id, request.name());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BoardResponse(board.getId(), board.getName(),
                        new UserDTO(board.getUser().getId(), board.getUser().getUsername()),
                        board.getPins().stream().map(pin -> new PinDTO(pin.getId(), pin.getUserId(), pin.getMediaId())).toList())
                );
    }

    @Operation(summary = "Fetch board by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully fetch board",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Board.class))),
        @ApiResponse(responseCode = "404", description = "Board not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> findById(
        @Parameter(description = "Id of the board to be search", required = true)
        @PathVariable Long id
    ){
        Board board = boardService.findById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new BoardResponse(board.getId(), board.getName(),
                        new UserDTO(board.getUser().getId(), board.getUser().getUsername()),
                        !board.getPins().isEmpty() ?
                                board.getPins().stream().map(pin -> new PinDTO(pin.getId(), pin.getUserId(), pin.getMediaId())).toList() :
                                Collections.emptyList()
                        )
                );
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
        boardService.deleteIfUserMatches(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
