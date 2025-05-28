package com.example.demo.exceptions;

/**
 * Exception thrown when a JWT token has an invalid format
 */
public class InvalidJwtFormatException extends JwtException {
    
    public InvalidJwtFormatException() {
        super("Invalid JWT format");
    }
    
    public InvalidJwtFormatException(Throwable cause) {
        super("Invalid JWT format", cause);
    }
}