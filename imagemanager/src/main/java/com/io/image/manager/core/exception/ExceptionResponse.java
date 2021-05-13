package com.io.image.manager.core.exception;

import lombok.Value;

import java.time.Instant;

@Value
public class ExceptionResponse {
    Instant instant;
    String message;
}
