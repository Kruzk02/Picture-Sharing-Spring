package com.app.exception.sub;

public class CommentIsEmptyException extends RuntimeException {
    public CommentIsEmptyException(String message) {
        super(message);
    }
}
