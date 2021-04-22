package com.io.image.manager.exceptions;

public class ImageNotFoundException extends Exception {
    public ImageNotFoundException() {
        super();
    }
    public ImageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public ImageNotFoundException(String message) {
        super(message);
    }
    public ImageNotFoundException(Throwable cause) {
        super(cause);
    }
}