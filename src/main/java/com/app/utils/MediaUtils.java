package com.app.utils;

import com.app.Model.MediaType;
import com.app.exception.sub.MediaNotSupportException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Component
public class MediaUtils {

    /**
     * Extracts the extension from the filename.
     * @param filename the full filename
     * @return the file extension
     */
    public String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex <= 0 || lastDotIndex == filename.length() - 1) {
            throw new IllegalArgumentException("Filename must have a valid extension");
        }

        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Validates if the given extension is supported
     * @param extension the file extension to validate
     * @return true if the extension is supported, false otherwise
     */
    public boolean isValidFormat(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            throw new IllegalArgumentException("Extension cannot be null or empty");
        }
        return MediaType.IMAGE.isFormatValid(extension) || MediaType.VIDEO.isFormatValid(extension);
    }

    /**
     * Extracts the filename without the extension
     * @param filename the full filename
     * @return the base filename
     */
    public String getBaseFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex <= 0) {
            return filename;
        }
        return filename.substring(0, lastDotIndex);
    }

    /**
     * Generate unique name for file
     * @param originFilename the full filename
     * @return the unique name
     */
    public String generateUniqueFilename(String originFilename) {
        String extension = getFileExtension(originFilename);
        if (!isValidFormat(extension)) {
            throw new MediaNotSupportException("Media format not supported");
        }

        String uniqueId = UUID.randomUUID().toString();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return uniqueId + "_" + timestamp + "." + extension;
    }

    public Long sizeFromFile(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFilePath() {
        try {
            Resource resource = new FileSystemResource("video/");
            return new File(String.valueOf(resource.getFile())).getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] readByRange(Path path, long start, long end) {
        try (SeekableByteChannel byteChannel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) (end - start + 1));
            byteChannel.position(start);
            byteChannel.read(buffer);
            return buffer.array();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getFileSize(String filename) {
        return Optional.of(filename)
                .map(file -> Paths.get(getFilePath(), file))
                .map(this::sizeFromFile)
                .orElse(0L);
    }
}
