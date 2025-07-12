package com.app.Controller;

import com.app.DTO.request.LoginUserRequest;
import com.app.DTO.request.RegisterUserRequest;
import com.app.DTO.request.TokenRequest;
import com.app.DTO.request.UpdateUserRequest;
import com.app.DTO.response.*;
import com.app.Jwt.JwtProvider;
import com.app.Model.Board;
import com.app.Model.Notification;
import com.app.Model.Pin;
import com.app.Model.User;
import com.app.Service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequestMapping("/api/users")
@RestController
public class UserController {

  private final UserService userService;
  private final BoardService boardService;
  private final PinService pinService;
  private final FollowerService followerService;
  private final NotificationService notificationService;

  @Qualifier(value = "jwtAccessToken")
  private final JwtProvider jwtAccessToken;

  @Qualifier(value = "jwtRefreshToken")
  private final JwtProvider jwtRefreshToken;

  private final RedisTemplate<String, Object> redisTemplate;

  @Autowired
  public UserController(
      UserService userService,
      BoardService boardService,
      PinService pinService,
      FollowerService followerService,
      NotificationService notificationService,
      JwtProvider jwtAccessToken,
      JwtProvider jwtRefreshToken,
      RedisTemplate<String, Object> redisTemplate) {
    this.userService = userService;
    this.boardService = boardService;
    this.pinService = pinService;
    this.followerService = followerService;
    this.notificationService = notificationService;
    this.jwtAccessToken = jwtAccessToken;
    this.jwtRefreshToken = jwtRefreshToken;
    this.redisTemplate = redisTemplate;
  }

  @Operation(summary = "Login account")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully Login",
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
        @RequestBody LoginUserRequest request, @RequestParam(defaultValue = "false") boolean isRemember, HttpServletResponse response){
        User user = userService.login(request);

        String accessToken = jwtAccessToken.generateToken(TokenRequest.builder()
                .username(user.getUsername())
                .isRemember(isRemember)
                .build()
        );

        if (isRemember) {
            String refreshToken = jwtRefreshToken.generateToken(TokenRequest.builder()
                    .username(user.getUsername())
                    .isRemember(true)
                    .build()
            );

            Cookie cookie = new Cookie("refresh_token", refreshToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("http://localhost:8080/api/users/refresh");
            cookie.setMaxAge(30 * 24 * 60 * 60);

            response.addCookie(cookie);

        }

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new JwtResponse(accessToken));
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

        String token = jwtAccessToken.generateToken(TokenRequest.builder()
                .username(user.getUsername())
                .isRemember(false)
                .build()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new JwtResponse(token));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshAccessToken(@CookieValue(name = "refresh_token") String refreshToken, @RequestHeader(name = "Authorization") String oldAccessToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        oldAccessToken = oldAccessToken.substring(7);

        String username = jwtRefreshToken.extractUsernameFromToken(refreshToken);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!jwtRefreshToken.validateToken(refreshToken, username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        long accessTokenExpiry = jwtAccessToken.extractExpiration(oldAccessToken).getTime() - System.currentTimeMillis();
        System.out.println(accessTokenExpiry);
        redisTemplate.opsForValue().set("blacklist:" + oldAccessToken, "blacklisted", accessTokenExpiry);

        String accessToken = jwtAccessToken.generateToken(TokenRequest.builder()
                .username(username)
                .build());
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new JwtResponse(token));
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtResponse> refreshAccessToken(
      @CookieValue(name = "refresh_token") String refreshToken,
      @RequestHeader(name = "Authorization") String oldAccessToken) {
    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    oldAccessToken = oldAccessToken.substring(7);

    String username = jwtRefreshToken.extractUsernameFromToken(refreshToken);
    if (username == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    if (!jwtRefreshToken.validateToken(refreshToken, username)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    long accessTokenExpiry =
        jwtAccessToken.extractExpiration(oldAccessToken).getTime() - System.currentTimeMillis();
    System.out.println(accessTokenExpiry);
    redisTemplate
        .opsForValue()
        .set("blacklist:" + oldAccessToken, "blacklisted", accessTokenExpiry);

    String accessToken =
        jwtAccessToken.generateToken(
            TokenRequest.builder().claims(new HashMap<>()).username(username).build());
    return ResponseEntity.status(HttpStatus.OK)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new JwtResponse(accessToken));
  }

  @Operation(summary = "Get username from token")
  @GetMapping("/get-username")
  public ResponseEntity<Map<String, Object>> getUsernameFromToken() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    Map<String, Object> map = new HashMap<>();
    map.put("username", username);
    return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(map);
  }

  @Operation(summary = "Update user info")
  @PutMapping("/update-user")
  public ResponseEntity<UserResponse> updateUser(@ModelAttribute UpdateUserRequest request) {

    User user = userService.update(request);

    return ResponseEntity.status(HttpStatus.OK)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getMedia().getId(),
                user.getBio(),
                user.getGender()));
  }

  @GetMapping("/user-details")
  public ResponseEntity<UserResponse> getFullUserDetails() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userService.findFullUserByUsername(authentication.getName());

    return ResponseEntity.status(HttpStatus.OK)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getMedia().getId(),
                user.getBio(),
                user.getGender()));
  }

  @GetMapping("/{id}/followers")
  public ResponseEntity<List<UserResponse>> getAllFollowersByUserId(
      @PathVariable Long id, @RequestParam(defaultValue = "10") int limit) {
    return ResponseEntity.status(HttpStatus.OK)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            followerService.getAllFollowingByUserId(id, limit).stream()
                .map(
                    user ->
                        new UserResponse(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getMedia().getId(),
                            user.getBio(),
                            user.getGender()))
                .toList());
  }

  @PostMapping("/{id}/followers")
  public ResponseEntity<FollowerResponse> followUser(@PathVariable Long id) {
    return ResponseEntity.status(HttpStatus.OK)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new FollowerResponse(followerService.followUser(id).getFollowingId()));
  }

  @DeleteMapping("/{id}/followers")
  public ResponseEntity<Void> unFollow(@PathVariable Long id) {
    followerService.unfollowUser(id);
    return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).build();
  }

  @Operation(summary = "Fetch all board by user id")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully fetch all board by user id",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Board.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{userId}/boards")
  public ResponseEntity<List<BoardResponse>> findAllByUserId(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    List<Board> boards = boardService.findAllByUserId(userId, limit, offset);
    return ResponseEntity.status(HttpStatus.OK)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            boards.parallelStream()
                .map(
                    board ->
                        new BoardResponse(
                            board.getId(),
                            board.getName(),
                            new UserDTO(board.getUser().getId(), board.getUser().getUsername()),
                            board.getPins().stream()
                                .skip(offset)
                                .limit(limit)
                                .map(
                                    pin ->
                                        new PinDTO(pin.getId(), pin.getUserId(), pin.getMediaId()))
                                .toList()))
                .toList());
  }

  @GetMapping("/{userId}/pins")
  public ResponseEntity<List<PinResponse>> findPinByUserId(
      @Parameter(description = "id of the user whose pin are to be retrieved", required = true)
          @PathVariable
          Long userId,
      @Parameter(description = "Maximum number of pins to be retrieved")
          @RequestParam(defaultValue = "10")
          int limit,
      @Parameter(description = "Offset for pagination, indicating the starting point")
          @RequestParam(defaultValue = "0")
          int offset) {
    List<Pin> pins = pinService.findPinByUserId(userId, limit, offset);
    return ResponseEntity.status(HttpStatus.OK)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            pins.stream()
                .map(
                    pin ->
                        new PinResponse(
                            pin.getId(),
                            pin.getUserId(),
                            pin.getDescription(),
                            pin.getMediaId(),
                            new ArrayList<>(),
                            pin.getCreatedAt()))
                .toList());
  }

  @GetMapping("/notification")
  public ResponseEntity<List<NotificationResponse>> findByUserId(
      @RequestParam(defaultValue = "false") boolean fetchUnread,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    List<Notification> notifications = notificationService.findByUserId(limit, offset, fetchUnread);
    return ResponseEntity.status(HttpStatus.OK)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            notifications.stream()
                .map(
                    notification ->
                        new NotificationResponse(
                            notification.getId(),
                            notification.getUserId(),
                            notification.getMessage(),
                            notification.isRead(),
                            notification.getCreatedAt()))
                .toList());
  }

  @PostMapping("/sse-notification")
  public SseEmitter stream() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return notificationService.createEmitter(
        userService.findFullUserByUsername(authentication.getName()).getId());
  }

  @Operation(summary = "Verify user account")
  @GetMapping("/verify")
  public ResponseEntity<VerifyAccountResponse> verifyAccount(@RequestParam String token) {
    userService.verifyAccount(token);
    return ResponseEntity.status(HttpStatus.OK)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new VerifyAccountResponse("Account verified successfully."));
  }
}
