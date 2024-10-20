package com.app.Controller;

import com.app.DTO.LoginDTO;
import com.app.DTO.RegisterDTO;
import com.app.DTO.UpdateUserDTO;
import com.app.Jwt.JwtProvider;
import com.app.Model.User;
import com.app.Service.UserService;
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

    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(@RequestBody LoginDTO loginDTO){
        User user = userService.login(loginDTO);
        Map<String,String> response = new HashMap<>();

        String token = jwtProvider.generateToken(user.getUsername());
        response.put("status","ok");
        response.put("token",token);
        response.put("timestamp", String.valueOf(LocalDateTime.now()));
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterDTO registerDTO) {
        User existingEmail = userService.findUserByEmail(registerDTO.getEmail());
        User existingUsername = userService.findUserByUsername(registerDTO.getUsername());

        Map<String, Object> response = new HashMap<>();

        if (existingEmail != null) {
            response.put("status", "error");
            response.put("message", "Email is already taken.");
            response.put("timestamp", String.valueOf(LocalDateTime.now()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (existingUsername != null) {
            response.put("status", "error");
            response.put("message", "Username is already taken.");
            response.put("timestamp", String.valueOf(LocalDateTime.now()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        userService.register(registerDTO);
        response.put("status", "ok");
        response.put("message", "successfully registered");
        response.put("timestamp", String.valueOf(LocalDateTime.now()));
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @GetMapping("/get-username")
    public ResponseEntity<String> getUsernameFromToken(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return ResponseEntity.ok(username);
    }

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
