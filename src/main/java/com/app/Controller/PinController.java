package com.app.Controller;

import com.app.DTO.request.UploadPinRequest;
import com.app.DTO.response.GetAllPinResponse;
import com.app.DTO.response.GetCommentResponse;
import com.app.DTO.response.GetPinResponse;
import com.app.DTO.response.UploadPinResponse;
import com.app.Model.Comment;
import com.app.Model.Pin;
import com.app.Model.User;
import com.app.Service.PinService;
import com.app.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    public PinController(PinService pinService, UserService userService) {
        this.pinService = pinService;
        this.userService = userService;
    }

    @Operation(summary = "Get all Pins")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully get all pins",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Pin.class))}),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<GetAllPinResponse> getAllPins(
        @Parameter(description = "Pagination offset for the results")
        @RequestParam(value = "offset", defaultValue = "0") @Min(0) int offset
    ) {
        List<Pin> pins = pinService.getAllPins(offset);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new GetAllPinResponse(pins));
    }

    @Operation(summary = "Upload a pin")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pin uploaded successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Pin.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "413", description = "File is larger than 10MB",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "415", description = "File type is not an image",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/upload")
    public ResponseEntity<UploadPinResponse> upload(
            @ModelAttribute UploadPinRequest request,
            @Parameter(description = "File to upload")
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(authentication.getName());
        pinService.asyncData(user.getId(), request, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UploadPinResponse("Your pin is being processing"));
    }

    @Operation(summary = "Download the pin with specified by the ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully downloaded the pin",
            content = @Content(mediaType = "application/octet-stream",schema =  @Schema(implementation = InputStreamResource.class))),
        @ApiResponse(responseCode = "404", description = "Pin not found",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> download(
        @Parameter(description = "Id of the pin to be downloaded", required = true) @PathVariable Long id
    )throws IOException {

        Pin pin = pinService.findById(id);
        Path filePath = Paths.get(pin.getImage_url());
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + pin.getFileName());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(Files.size(filePath))
                .body(resource);
    }

    @Operation(summary = "Fetch a pin by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the pin",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Pin.class))),
        @ApiResponse(responseCode = "404", description = "Pin not found",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GetPinResponse> getPinById(
        @Parameter(description = "id of the pin to be searched", required = true)
        @PathVariable Long id
    ){
        Pin pin = pinService.findById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new GetPinResponse(pin.getUserId(),pin.getImage_url(),pin.getDescription()));
    }

    @Operation(summary = "Find all comment by pin id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully get all comment",
            content = @Content(mediaType = "application/json",schema = @Schema(implementation = Comment.class))),
        @ApiResponse(responseCode = "404", description = "Pin not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}/comment")
    public ResponseEntity<GetCommentResponse> getAllCommentByPinId(
        @Parameter(description = "id of the pin to be searched", required = true)
        @PathVariable Long id
    ){
        List<Comment> comments = pinService.getAllCommentByPinId(id);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new GetCommentResponse(comments));
    }

    @Operation(summary = "Get a pin photo by pin id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found pin photo",
            content = @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Resource.class))),
        @ApiResponse(responseCode = "404", description = "Pin not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}/photo")
    public ResponseEntity<Resource> getPhotoByPinId(
        @Parameter(description = "Id of the pin photo to be search", required = true)
        @PathVariable Long id
    ) throws IOException {
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

    @Operation(summary = "Delete an pin by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success delete an ebook", content = { @Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Ebook not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePinById(@PathVariable Long id) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUsername(authentication.getName());
        pinService.deleteIfUserMatches(user,id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .contentType(MediaType.APPLICATION_JSON)
                .build();
    }

}
