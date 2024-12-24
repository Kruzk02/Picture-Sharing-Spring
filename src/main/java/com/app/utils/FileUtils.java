package com.app.utils;

import com.app.Model.Media;
import com.app.Model.MediaType;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
public class FileUtils {

    /**
     * Check if the directory for media type exists based on the provided file extension.
     * If the directory does not exist, it is created
     * @param extension the file extension
     * @return Path the directory path where the file will be stored.
     * @throws IOException If an I/O error occurs while checking or creating the directory.
     */
    private Path checkFolderExistsAndCreate(String extension) throws IOException {
        Path path;
        if (MediaType.IMAGE.isFormatValid(extension)) {
            path = Paths.get("image");
            log.info("Media type identified as IMAGE. Path: {}", path);
        } else if (MediaType.VIDEO.isFormatValid(extension)) {
            path = Paths.get("video");
            log.info("Media type identified as VIDEO. Path: {}", path);
        } else {
            log.error("Invalid media type for extension: {}", extension);
            throw new IllegalArgumentException("Invalid media type.");
        }

        if (!Files.exists(path)) {
            log.info("Directory does not exist. Creating directory: {}", path);
            Files.createDirectories(path);
            log.info("Directory created successfully: {}", path);
        } else {
            log.info("Directory already exists: {}", path);
        }
        return path;
    }

    /**
     * Saves the provided file to the appropriate directory based on its extension.
     *
     * @param file The file to be saved
     * @param filename The name of the file to be saved.
     * @param extension The file extension used to determine the media type.
     * @return A CompletableFuture that runs the save operation asynchronously.
     * @throws IllegalArgumentException If any of the input parameters are null.
     */
    public CompletableFuture<Void> save(MultipartFile file, String filename, String extension) {
        if (file == null || filename == null || extension == null) {
            throw new IllegalArgumentException("File, filename, and extension must not be null.");
        }

        return CompletableFuture.runAsync(() -> {
            try {
                Path folder = checkFolderExistsAndCreate(extension);
                Path filePath = folder.resolve(filename);
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                    log.info("File saved successfully to path: {}", filePath);
                }
            } catch (IOException e) {
                log.error("Error saving media file", e);
                throw new RuntimeException("Error saving media file: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Deletes the file with the given filename from the appropriate directory based on its extension.
     *
     * @param filename The name of the file to be deleted.
     * @param extension The file extension used to determine the media type.
     * @return A CompletableFuture that runs the delete operation asynchronously.
     * @throws IllegalArgumentException If any of the input parameters are null.
     */
    public CompletableFuture<Void> delete(String filename, String extension) {
        if (filename == null || extension == null) {
            throw new IllegalArgumentException("File, filename, and extension must not be null.");
        }

        return CompletableFuture.runAsync(() -> {
            try {
                Path folder = checkFolderExistsAndCreate(extension);
                Path filePath = folder.resolve(filename);

                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    log.info("File deleted successfully: {}", filePath);
                } else {
                    log.warn("File not found for deletion: {}", filePath);
                }
            } catch (IOException e) {
                log.error("Error deleting media file: {}", filename, e);
                throw new RuntimeException("Error deleting media file: " + filename, e);
            }
        });
    }
}
