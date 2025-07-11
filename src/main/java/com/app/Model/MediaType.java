package com.app.Model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum MediaType {
  IMAGE("JPEG", "JPG", "PNG", "GIF"),
  VIDEO("MP4");

  private final Set<String> validFormat;

  MediaType(String... extensions) {
    this.validFormat = new HashSet<>(Arrays.asList(extensions));
  }

  public boolean isFormatValid(String extension) {
    for (String format : validFormat) {
      if (format.equalsIgnoreCase(extension)) {
        return true;
      }
    }
    return false;
  }

  public static MediaType fromExtension(String extension) {
    for (MediaType type : MediaType.values()) {
      if (type.isFormatValid(extension)) {
        return type;
      }
    }
    throw new IllegalArgumentException("No matching MediaType for extension: " + extension);
  }
}
