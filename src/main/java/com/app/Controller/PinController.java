package com.app.Controller;

import com.app.DTO.PinDTO;
import com.app.Jwt.JwtProvider;
import com.app.Model.Pin;
import com.app.Model.User;
import com.app.Service.PinService;
import com.app.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/pin")
public class PinController {

    private final PinService pinService;
    private final UserService userService;
    private final JwtProvider jwtProvider;

    @Autowired
    public PinController(PinService pinService, UserService userService, JwtProvider jwtProvider) {
        this.pinService = pinService;
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @GetMapping
    public ResponseEntity<List<Pin>> getAllPins(){
        try {
            List<Pin> pins = pinService.getAllPins();
            return ResponseEntity.ok(pins);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @ModelAttribute PinDTO pinDTO,
            @RequestParam("file")MultipartFile file,
            @RequestHeader("Authorization") String authHeader){

        try{

            String token = extractToken(authHeader);
            if(token != null){

                String username = jwtProvider.extractUsername(token);
                User user = userService.findUserByUsername(username);

                pinDTO.setUser(user);

                Pin pin = pinService.save(pinDTO,file);
                return ResponseEntity.status(HttpStatus.CREATED).body(pin);
            }else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Authorization header");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> download(@PathVariable Long id){
        try{
            Pin pin = pinService.findById(id);
            Path filePath = Paths.get(pin.getImage_url());
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=" + pin.getFileName()+".png");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(Files.size(filePath))
                    .body(resource);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPinById(@PathVariable Long id){
        try{
            Pin pin = pinService.findById(id);
            return ResponseEntity.ok(pin);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePinById(@PathVariable Long id){
        try{
            pinService.deleteById(id);
            return ResponseEntity.ok().build();
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
