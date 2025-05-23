package com.example.demo.exceptions;

public class InvalidTrainingDataException extends RuntimeException {
    public InvalidTrainingDataException(String message) {
        super(message);
    }
}
