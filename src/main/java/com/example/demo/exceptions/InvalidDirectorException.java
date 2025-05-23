package com.example.demo.exceptions;

public class InvalidDirectorException extends RuntimeException {
    public InvalidDirectorException(String message) {
        super(message);
    }
}
