package com.microjob.microjob_exchange.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    // This method catches all exceptions not already handled by @ResponseStatus (like 404, 403)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {

        // --- CRITICAL PRINT STATEMENT ---
        System.err.println("\n\n=============== START OF CRITICAL ERROR TRACE ===============");
        System.err.println("Request URL: " + request.getDescription(false));
        System.err.println("Error Type: " + ex.getClass().getSimpleName());
        System.err.println("Message: " + ex.getMessage());
        ex.printStackTrace(System.err); // Print the full stack trace to the console
        System.err.println("================ END OF CRITICAL ERROR TRACE ===============\n");

        // Return a generic 500 status to the client
        return new ResponseEntity<>("Internal Server Error. Check server logs for details.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}