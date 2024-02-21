package com.app.Controller;

import com.app.DTO.LoginDTO;
import com.app.DTO.RegisterDTO;
import com.app.Model.User;
import com.app.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO){
        try {
            User user = userService.login(loginDTO);
            return ResponseEntity.status(200).body(user);
        }catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid Username or password.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO registerDTO){
        User existingEmail = userService.findUserByEmail(registerDTO.getEmail());

        if(existingEmail != null){
            return ResponseEntity.status(400).body("Email is already taken.");
        }

        User user = userService.register(registerDTO);
        return ResponseEntity.status(200).body(user);
    }
}
