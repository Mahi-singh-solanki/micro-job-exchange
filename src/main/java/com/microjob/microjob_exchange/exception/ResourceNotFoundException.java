package com.microjob.microjob_exchange.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Optional: Include another constructor for chaining exceptions
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

