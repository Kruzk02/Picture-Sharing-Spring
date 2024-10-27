package com.app.Controller;

import com.app.DTO.LoginDTO;
import com.app.DTO.RegisterDTO;
import com.app.DTO.UpdateUserDTO;
import com.app.Jwt.JwtProvider;
import com.app.Model.User;
import com.app.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/api")
@RestController
public class UserController {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    @Autowired
    public UserController(UserService userService, JwtProvider jwtProvider) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @Operation(summary = "Login account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully Login",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Login Data",required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginDTO.class))
        )
        @RequestBody LoginDTO loginDTO
    ){
        User user = userService.login(loginDTO);
        Map<String,String> response = new HashMap<>();

        String token = jwtProvider.generateToken(user.getUsername());
        response.put("status","ok");
        response.put("token",token);
        response.put("timestamp", String.valueOf(LocalDateTime.now()));
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @Operation(summary = "Register user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Success create account",content = {@Content(mediaType = "application/json",schema = @Schema(implementation = User.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "409", description = "Username or email is already taken", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterDTO registerDTO) {
        User existingEmail = userService.findUserByEmail(registerDTO.getEmail());
        User existingUsername = userService.findUserByUsername(registerDTO.getUsername());

        Map<String, Object> response = new HashMap<>();

        if (existingEmail != null) {
            response.put("status", "error");
            response.put("message", "Email is already taken.");
            response.put("timestamp", String.valueOf(LocalDateTime.now()));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        if (existingUsername != null) {
            response.put("status", "error");
            response.put("message", "Username is already taken.");
            response.put("timestamp", String.valueOf(LocalDateTime.now()));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        userService.register(registerDTO);
        response.put("status", "ok");
        response.put("message", "successfully registered");
        response.put("timestamp", String.valueOf(LocalDateTime.now()));
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @Operation(summary = "Get username from token")
    @GetMapping("/get-username")
    public ResponseEntity<String> getUsernameFromToken(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(username);
    }

    @Operation(summary = "Update user info")
    @PutMapping("/update-user-information")
    public ResponseEntity<?> updateUserInformation( @RequestBody UpdateUserDTO updateUserDTO){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findUserByUsername(username);

        user.setUsername(updateUserDTO.getUsername());
        user.setEmail(updateUserDTO.getEmail());
        user.setPassword(updateUserDTO.getPassword());
        userService.update(user);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
