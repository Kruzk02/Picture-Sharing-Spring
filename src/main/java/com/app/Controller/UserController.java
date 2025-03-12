package com.app.Controller;

import com.app.DTO.request.LoginUserRequest;
import com.app.DTO.request.RegisterUserRequest;
import com.app.DTO.request.UpdateUserRequest;
import com.app.DTO.response.*;
import com.app.Jwt.JwtProvider;
import com.app.Model.Board;
import com.app.Model.User;
import com.app.Service.BoardService;
import com.app.Service.FollowerService;
import com.app.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/users")
@RestController
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final BoardService boardService;
    private final FollowerService followerService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "Login account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully Login",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Login Data",required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginUserRequest.class))
        )
        @RequestBody LoginUserRequest request
            ){
        User user = userService.login(request);

        String token = jwtProvider.generateToken(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new JwtResponse(token));
    }

    @Operation(summary = "Register user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Success create account",content = {@Content(mediaType = "application/json",schema = @Schema(implementation = User.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "409", description = "Username or email is already taken", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@RequestBody RegisterUserRequest request) {
        User user = userService.register(request);

        String token = jwtProvider.generateToken(user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new JwtResponse(token));
    }

    @Operation(summary = "Get username from token")
    @GetMapping("/get-username")
    public ResponseEntity<Map<String,Object>> getUsernameFromToken(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Map<String,Object> map = new HashMap<>();
        map.put("username",username);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(map);
    }

    @Operation(summary = "Update user info")
    @PutMapping("/update-user")
    public ResponseEntity<UserResponse> updateUser(
            @ModelAttribute UpdateUserRequest request) {

        User user = userService.update(request);

        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new UserResponse(
                user.getId(),user.getUsername(),user.getEmail(),
                user.getMedia().getId(), user.getBio(), user.getGender()
            ));
    }

    @GetMapping("/user-details")
    public ResponseEntity<UserResponse> getFullUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findFullUserByUsername(authentication.getName());

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UserResponse(
                        user.getId(),user.getUsername(),user.getEmail(),
                        user.getMedia().getId(), user.getBio(), user.getGender()
                ));
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<List<UserResponse>> getAllFollowersByUserId(@PathVariable Long id, @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(followerService.getAllFollowingByUserId(id, limit).stream().map(user ->
                    new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getMedia().getId(), user.getBio(), user.getGender())).toList()
                );
    }

    @PostMapping("/{id}/followers")
    public ResponseEntity<FollowerResponse> addFollower(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new FollowerResponse(followerService.addFollowerToUser(id).getFollowingId()));
    }

    @DeleteMapping("/{id}/followers")
    public ResponseEntity<Void> removeFollower(@PathVariable Long id) {
        followerService.removeFollowerFromUser(id);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .build();
    }

    @Operation(summary = "Fetch all board by user id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetch all board by user id",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Board.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{userId}/boards")
    public ResponseEntity<List<BoardResponse>> findAllByUserId(@PathVariable Long userId, @RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "0") int offset) {
        List<Board> boards = boardService.findAllByUserId(userId, limit ,offset);
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(boards.parallelStream()
                        .map(board -> new BoardResponse(
                                board.getId(),
                                board.getName(),
                                new UserDTO(board.getUser().getId(), board.getUser().getUsername()),
                                board.getPins().stream()
                                        .skip(offset)
                                        .limit(limit)
                                        .map(pin -> new PinDTO(pin.getId(), pin.getUserId(), pin.getMediaId()))
                                        .toList()
                        ))
                        .toList()
                );
    }

    @Operation(summary = "Verify user account")
    @GetMapping("/verify")
    public ResponseEntity<VerifyAccountResponse> verifyAccount(@RequestParam String token) {
        userService.verifyAccount(token);
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new VerifyAccountResponse("Account verified successfully."));
    }
}
