package com.app.exception.sub;

public class PinIsEmptyException extends RuntimeException {
    public PinIsEmptyException(String message) {
        super(message);
    }
}
