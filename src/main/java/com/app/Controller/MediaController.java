package com.app.Controller;

import com.app.Model.Media;
import com.app.Model.MediaType;
import com.app.Service.MediaService;
import com.app.storage.MediaManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/media")
@AllArgsConstructor
public class MediaController {

  private final MediaService mediaService;

  @Operation(summary = "Get a image or video by media id")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Found media",
            content =
                @Content(
                    mediaType = "application/octet-stream",
                    schema = @Schema(implementation = Resource.class))),
        @ApiResponse(responseCode = "404", description = "Media not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<Resource> getMediaById(
      @Parameter(description = "Id of the media to be search", required = true) @PathVariable
          Long id,
      @RequestHeader(value = "Range", required = false) String rangeHeader)
      throws IOException {
    Media media = mediaService.findById(id);
    if (media.getMediaType().equals(MediaType.IMAGE)) {
      Path imagePath = Paths.get("image/", media.getUrl());

      String mineType = Files.probeContentType(imagePath);
      if (mineType == null) {
        mineType = org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
      }

      Resource resource = new FileSystemResource(imagePath);
      return ResponseEntity.status(HttpStatus.OK)
          .contentType(org.springframework.http.MediaType.parseMediaType(mineType))
          .body(resource);
    } else {
      Path videoPath = Paths.get(MediaManager.getFilePath(), media.getUrl());
      long size = MediaManager.getFileSize(media.getUrl());

      long start = 0;
      long end = size - 1;

      if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
        String[] ranges = rangeHeader.substring(6).split("-");
        start = Long.parseLong(ranges[0]);
        if (ranges.length > 1) {
          end = Long.parseLong(ranges[1]);
        }
      }

      if (start >= size || end >= size) {
        return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
            .header("Content-Range", "bytes */" + size)
            .build();
      }

      byte[] videoData = MediaManager.readByRange(videoPath, start, end);

      return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
          .header("Content-Type", Files.probeContentType(videoPath))
          .header("Accept-Ranges", "bytes")
          .header("Content-Range", "bytes " + start + "-" + end + "/" + size)
          .contentLength(videoData.length)
          .body(new ByteArrayResource(videoData));
    }
  }
}
