package com.app.Model;

import lombok.Getter;

import java.util.Set;

@Getter
public enum MediaType {
    IMAGE(Set.of("jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp", "svg", "heic", "heif")),
    VIDEO(Set.of("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "3gp", "mpeg", "mpg"));

    private final Set<String> formats;

    MediaType(Set<String> formats) {
        this.formats = formats;
    }

    public boolean isValidFormat(String format) {
        return formats.contains(format.toLowerCase());
    }
}
