package com.example.demo.exceptions;

public class UnauthorizedImportException extends RuntimeException {
    public UnauthorizedImportException(String message) {
        super(message);
    }
}
