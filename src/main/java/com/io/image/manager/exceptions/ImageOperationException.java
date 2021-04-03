package com.io.image.manager.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

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