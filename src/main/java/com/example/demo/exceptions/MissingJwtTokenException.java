package com.example.demo.exceptions;

/**
 * Exception thrown when a JWT token is missing or empty
 */
public class MissingJwtTokenException extends JwtException {
    
    public MissingJwtTokenException() {
        super("JWT token is missing or empty");
    }
    
    public MissingJwtTokenException(Throwable cause) {
        super("JWT token is missing or empty", cause);
    }
}