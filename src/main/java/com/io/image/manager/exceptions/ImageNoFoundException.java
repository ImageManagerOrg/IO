package com.io.image.manager.exceptions;

public class ImageNoFoundException extends Exception {
    public ImageNoFoundException() {
        super();
    }
    public ImageNoFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public ImageNoFoundException(String message) {
        super(message);
    }
    public ImageNoFoundException(Throwable cause) {
        super(cause);
    }
}