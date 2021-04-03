package com.io.image.manager.exceptions;

public class ImageOperationException extends Exception {
    public ImageOperationException() {
        super();
    }
    public ImageOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    public ImageOperationException(String message) {
        super(message);
    }
    public ImageOperationException(Throwable cause) {
        super(cause);
    }
}