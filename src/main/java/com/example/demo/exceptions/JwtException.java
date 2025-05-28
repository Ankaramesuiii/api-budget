package com.example.demo.exceptions;

/**
 * Base exception class for JWT token related issues
 */
public class JwtException extends RuntimeException {
    
    public JwtException(String message) {
        super(message);
    }
    
    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }
}