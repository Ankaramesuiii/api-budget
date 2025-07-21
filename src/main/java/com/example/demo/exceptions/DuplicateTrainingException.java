package com.example.demo.exceptions;

public class DuplicateTrainingException extends RuntimeException {
    public DuplicateTrainingException(String message) {
        super(message);
    }
}
