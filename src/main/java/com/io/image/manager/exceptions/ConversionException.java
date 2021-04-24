package com.io.image.manager.exceptions;

public class ConversionException extends Exception {
    public ConversionException() {
        super();
    }
    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
    public ConversionException(String message) {
        super(message);
    }
    public ConversionException(Throwable cause) {
        super(cause);
    }
}