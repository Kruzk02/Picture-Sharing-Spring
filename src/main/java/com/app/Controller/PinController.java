package com.app.Controller;

import com.app.DTO.PinDTO;
import com.app.Model.Comment;
import com.app.Model.Pin;
import com.app.Model.User;
import com.app.Service.PinService;
import com.app.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/pin")
public class PinController {

    private final PinService pinService;
    private final UserService userService;

    @Autowired
    public PinController(PinService pinService, UserService userService) {
        this.pinService = pinService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Pin>> getAllPins(){
        return ResponseEntity.ok(pinService.getAllPins());
    }

    @PostMapping("/upload")
    public ResponseEntity<Pin> upload(@ModelAttribute PinDTO pinDTO, @RequestParam("file")MultipartFile file) throws ExecutionException, InterruptedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body( pinService.asyncData(user,pinDTO,file).get());
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) throws IOException {
        Pin pin = pinService.findById(id);
        Path filePath = Paths.get(pin.getImage_url());
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + pin.getFileName() + ".png");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(Files.size(filePath))
                .body(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pin> getPinById(@PathVariable Long id){
        return ResponseEntity.ok(pinService.findById(id));
    }

    @GetMapping("/{id}/comment")
    public ResponseEntity<List<Comment>> getAllCommentByPinId(@PathVariable Long id){
        List<Comment> comments = pinService.getAllCommentByPinId(id);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<Resource> getPhotoByPinId(@PathVariable Long id) throws IOException {
        Pin pin = pinService.findById(id);
        Path filePath = Paths.get("upload/" + pin.getFileName());

        String mimeType = Files.probeContentType(filePath);
        if (mimeType == null) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        Resource resource = new FileSystemResource(filePath);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .body(resource);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePinById(@PathVariable Long id) throws IOException {
        pinService.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
