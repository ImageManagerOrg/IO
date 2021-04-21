package com.io.image.manager.core.exception;

import com.io.image.manager.exceptions.ImageNoFoundException;
import com.io.image.manager.exceptions.ImageOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ImageOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleImageOperationException(ImageOperationException exception) {
        return new ExceptionResponse(Instant.now(), exception.getMessage());
    }

    @ExceptionHandler(ImageNoFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleImageNotFoundException(ImageNoFoundException exception) {
        return new ExceptionResponse(Instant.now(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleException(Exception exception) {
        return new ExceptionResponse(Instant.now(), exception.getMessage());
    }

}
