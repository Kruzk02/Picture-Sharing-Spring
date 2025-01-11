package com.app.Controller;

import com.app.DTO.request.PinRequest;
import com.app.DTO.response.*;
import com.app.Model.Comment;
import com.app.Model.Pin;
import com.app.Model.SortType;
import com.app.Service.CommentService;
import com.app.Service.PinService;
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

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/pin")
@AllArgsConstructor
public class PinController {

    private final PinService pinService;
    private final CommentService commentService;

    private List<Pin> findAllPinHelper(int limit, int offset, SortType sortType) {
        return switch (sortType) {
            case NEWEST -> pinService.findNewestPin(limit, offset);
            case OLDEST -> pinService.findOldestPin(limit, offset);
            default -> pinService.getAllPins(offset);
        };
    }

    @Operation(summary = "Get all Pins")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully get all pins",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Pin.class))}),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<List<PinResponse>> getAllPins(
        @Parameter(description = "Sorting type for pins: NEWEST, OLDEST or DEFAULT")
        @RequestParam(defaultValue = "DEFAULT") SortType sortType,
        @Parameter(description = "Maximum number of pins to be retrieved")
        @RequestParam(defaultValue = "10") int limit,
        @Parameter(description = "Offset for pagination, indicating the starting point")
        @RequestParam(defaultValue = "0") int offset
    ) {
        if (limit <= 0 || offset < 0) {
            throw new IllegalArgumentException("Limit must be greater than 0 and offset must be non-negative.");
        }

        List<Pin> pins = findAllPinHelper(limit, offset, sortType);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(pins.stream().map(pin ->
                        new PinResponse(
                                pin.getId(),
                                pin.getUserId(),
                                pin.getDescription(),
                                pin.getMediaId(),
                                pin.getCreatedAt()
                        )).toList()
                );
    }

    @GetMapping("/{userId}/user")
    public ResponseEntity<List<PinResponse>> findPinByUserId(
        @Parameter(description = "id of the user whose pin are to be retrieved", required = true)
        @PathVariable Long userId,
        @Parameter(description = "Maximum number of pins to be retrieved")
        @RequestParam(defaultValue = "10") int limit,
        @Parameter(description = "Offset for pagination, indicating the starting point")
        @RequestParam(defaultValue = "0") int offset
    ) {
        List<Pin> pins = pinService.findPinByUserId(userId, limit, offset);
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(pins.stream().map(pin ->
                new PinResponse(
                    pin.getId(),
                    pin.getUserId(),
                    pin.getDescription(),
                    pin.getMediaId(),
                    pin.getCreatedAt()
                )).toList()
            );
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
    public ResponseEntity<PinResponse> upload(
            @ModelAttribute PinRequest request
    ) {
        Pin pin = pinService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new PinResponse(
                pin.getId(),
                pin.getUserId(),
                pin.getDescription(),
                pin.getMediaId(),
                pin.getCreatedAt()
            ));
    }

    @Operation(description = "Update an existing pin")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success update an pin",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PinResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Pin not found"),
        @ApiResponse(responseCode = "400", description = "Invalid Input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PinResponse> update(
        @PathVariable Long id,
        @ModelAttribute PinRequest request
    ) {
        Pin pin = pinService.update(id, request);
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new PinResponse(
                pin.getId(),
                pin.getUserId(),
                pin.getDescription(),
                pin.getMediaId(),
                pin.getCreatedAt())
            );
    }

    @Operation(summary = "Fetch a basic pin detail by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the pin",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Pin.class))),
        @ApiResponse(responseCode = "404", description = "Pin not found",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PinResponse> getPinById(
        @Parameter(description = "id of the pin to be searched", required = true)
        @PathVariable Long id, @RequestParam(defaultValue = "basic") String view
    ){
        Pin pin;
        if ("detail".equalsIgnoreCase(view)) {
            pin = pinService.findFullById(id);
        } else {
            pin = pinService.findBasicById(id);
        }

        if (pin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).build();
        }

        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new PinResponse(
                pin.getId(),
                pin.getUserId(),
                pin.getDescription(),
                pin.getMediaId(),
                pin.getCreatedAt()
            ));
    }

    private List<Comment> findCommentByPinIdHelper(Long pinId, int limit, int offset, SortType sortType) {
        return switch (sortType) {
            case NEWEST -> commentService.findNewestByPinId(pinId, limit, offset);
            case OLDEST -> commentService.findOldestByPinId(pinId, limit, offset);
            default -> commentService.findByPinId(pinId, limit, offset);
        };
    }

    @Operation(summary = "Find all comment by pin id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully get all comment",
            content = @Content(mediaType = "application/json",schema = @Schema(implementation = Comment.class))),
        @ApiResponse(responseCode = "404", description = "Pin not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}/comment")
    public ResponseEntity<List<CommentResponse>> getAllCommentByPinId(
        @Parameter(description = "id of the pin whose comment are to be retrieved", required = true)
        @PathVariable Long id,
        @Parameter(description = "Sorting type for comments: NEWEST, OLDEST or DEFAULT")
        @RequestParam(defaultValue = "DEFAULT") SortType sortType,
        @Parameter(description = "Maximum number of comments to be retrieved")
        @RequestParam(defaultValue = "10") int limit,
        @Parameter(description = "Offset for pagination, indicating the starting point")
        @RequestParam(defaultValue = "0") int offset
    ){
        if (limit <= 0 || offset < 0) {
            throw new IllegalArgumentException("Limit must be greater than 0 and offset must be non-negative.");
        }

        List<Comment> comments = findCommentByPinIdHelper(id, limit, offset, sortType);
        if (comments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(comments.stream().map(comment ->
                new CommentResponse(
                    comment.getId(),
                    comment.getContent(),
                    comment.getPinId(),
                    comment.getUserId(),
                    comment.getMediaId()))
                .toList()
            );
    }

    @Operation(summary = "Delete an pin by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Success delete an ebook", content = { @Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Ebook not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePinById(@PathVariable Long id) throws IOException {
        pinService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .contentType(MediaType.APPLICATION_JSON)
                .build();
    }

}
