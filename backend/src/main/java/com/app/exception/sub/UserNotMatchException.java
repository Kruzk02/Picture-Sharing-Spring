package com.app.exception.sub;

public class UserNotMatchException extends RuntimeException {
    public UserNotMatchException(String message) {
        super(message);
    }
}
