package com.example.demo.exceptions;

import java.util.Date;

/**
 * Exception thrown when a JWT token has expired
 */
public class JwtTokenExpiredException extends JwtException {
    
    private final Date expirationDate;
    
    public JwtTokenExpiredException(Date expirationDate) {
        super("Token expired at " + expirationDate);
        this.expirationDate = expirationDate;
    }
    
    public Date getExpirationDate() {
        return expirationDate;
    }
}