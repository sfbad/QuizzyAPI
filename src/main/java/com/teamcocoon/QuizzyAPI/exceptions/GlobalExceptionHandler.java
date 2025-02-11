package com.teamcocoon.QuizzyAPI.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends Exception{
    public GlobalExceptionHandler() {
        super();
    }
    @ExceptionHandler(AuthentificationException.class)
    public ResponseEntity<?> authentificationException(AuthentificationException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<?> authorizationException(AuthorizationException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(EntityNotFoundedException.class)
    public ResponseEntity<?> entityNotFoundedException(EntityNotFoundedException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(EntityAlreadyExists.class)
    public ResponseEntity<?> entityAlreadyExists(EntityAlreadyExists e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }


}
