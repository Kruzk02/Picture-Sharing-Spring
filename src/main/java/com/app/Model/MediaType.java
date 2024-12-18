package com.app.Model;

public enum MediaType {
    IMAGE("JPEG","PNG","GIF"),
    VIDEO("MP4");

    private final String[] validFormat;

    MediaType(String... validFormat) {
        this.validFormat = validFormat;
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
}
