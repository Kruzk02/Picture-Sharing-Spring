package com.app.exception.sub;

public class MediaNotFoundException extends RuntimeException {
  public MediaNotFoundException(String message) {
    super(message);
  }
}
