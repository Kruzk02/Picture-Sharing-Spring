package com.app.exception.sub;

public class VerificationTokenNotFoundException extends RuntimeException {
  public VerificationTokenNotFoundException(String message) {
    super(message);
  }
}
