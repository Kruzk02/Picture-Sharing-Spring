package com.app.Controller;

import com.app.DTO.LoginDTO;
import com.app.DTO.RegisterDTO;
import com.app.DTO.UpdateUserDTO;
import com.app.Jwt.JwtProvider;
import com.app.Model.User;
import com.app.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO){
        User user = userService.login(loginDTO);

        String token = jwtProvider.generateToken(user.getUsername());
        return ResponseEntity.status(200).body(token);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDTO registerDTO){

        User existingEmail = userService.findUserByEmail(registerDTO.getEmail());
        User existingUsername = userService.findUserByUsername(registerDTO.getUsername());

        if(existingEmail != null){
            return ResponseEntity.status(400).body("Email is already taken.");
        }

        if(existingUsername != null){
            return ResponseEntity.status(400).body("Username is already taken.");
        }

        userService.register(registerDTO);
        return ResponseEntity.status(200).body("successfully registered");
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
