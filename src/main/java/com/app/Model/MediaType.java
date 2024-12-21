package com.app.Model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public enum MediaType {
    IMAGE("JPEG","PNG","GIF"),
    VIDEO("MP4");

    private final Set<String> validFormat;

    MediaType(String... extensions) {
        this.validFormat = new HashSet<>(Arrays.asList(extensions));
    }

    public boolean isFormatValid(String file) {
        if (file == null || !file.contains(".")) {
            return false;
        }

        String extension = file.substring(file.lastIndexOf(".") + 1).toUpperCase();
        for (String format : validFormat) {
            if (format.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    public static MediaType getByExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return null;
        }

        String lowerCaseExtension = extension.toUpperCase();
        for (MediaType mediaType : MediaType.values()) {
            if (mediaType.validFormat.contains(lowerCaseExtension)) {
                return mediaType;
            }
        }
        return null;
    }
}
