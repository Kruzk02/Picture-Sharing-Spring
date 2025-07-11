package com.app.exception.sub;

public class TokenExpireException extends RuntimeException {
  public TokenExpireException(String message) {
    super(message);
  }
}
