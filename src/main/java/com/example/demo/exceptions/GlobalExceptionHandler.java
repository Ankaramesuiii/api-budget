package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // === Specific Handlers ===

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<String> handleMissingFilePart(MissingServletRequestPartException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Le fichier est requis mais manquant dans la requête.");
    }
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingFilePart(MissingServletRequestParameterException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Le paramètre requis '" + ex.getParameterName() + "' est manquant dans la requête.");
    }

    // === BAD REQUEST: All validation-related exceptions ===

    @ExceptionHandler({
            InvalidInputException.class,
            FileMissingException.class,
            InvalidDirectorException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(RuntimeException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    // === UNAUTHORIZED: Auth-related errors ===

    @ExceptionHandler({
            UnauthorizedException.class,
            LoginFailedException.class
    })
    public ResponseEntity<ErrorResponse> handleUnauthorized(RuntimeException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    // === CONFLICT: Duplicate users and registration failures ===

    @ExceptionHandler({
            UserAlreadyExistsException.class,
            RegistrationFailedException.class
    })
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(RuntimeException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.CONFLICT);
    }

    // === NOT FOUND: User not found ===

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    // === GLOBAL fallback ===

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {

        // Allow Server-Sent Events to bypass this
        if (request.getHeader("Accept") != null &&
                Objects.requireNonNull(request.getHeader("Accept")).contains("text/event-stream")) {
            return null;
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
