package com.microjob.microjob_exchange.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Tells Spring to return an HTTP 403 Forbidden status
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException(String message) {
        super(message);
    }
}

