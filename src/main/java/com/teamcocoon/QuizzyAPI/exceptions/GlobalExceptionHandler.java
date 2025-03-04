package com.teamcocoon.QuizzyAPI.exceptions;

import com.teamcocoon.QuizzyAPI.dtos.ErrorResponseDto;
import com.teamcocoon.QuizzyAPI.dtos.ExceptionsResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorResponseDto>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ErrorResponseDto> errors = new ArrayList<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            ErrorResponseDto errorResponse = new ErrorResponseDto(error.getField(), error.getDefaultMessage());
            errors.add(errorResponse);
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Gère l'exception d'authentification
    @ExceptionHandler(AuthentificationException.class)
    public ResponseEntity<ExceptionsResponseDTO> authentificationException(AuthentificationException e) {
        ExceptionsResponseDTO responseDTO = new ExceptionsResponseDTO(HttpStatus.UNAUTHORIZED, e.getMessage());
        return new ResponseEntity<>(responseDTO, HttpStatus.UNAUTHORIZED);
    }

    // Gère l'exception d'autorisation
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ExceptionsResponseDTO> authorizationException(AuthorizationException e) {
        ExceptionsResponseDTO responseDTO = new ExceptionsResponseDTO(HttpStatus.FORBIDDEN, e.getMessage());
        return new ResponseEntity<>(responseDTO, HttpStatus.FORBIDDEN);
    }

    // Gère l'exception lorsque l'entité n'est pas trouvée
    @ExceptionHandler(EntityNotFoundedException.class)
    public ResponseEntity<ExceptionsResponseDTO> entityNotFoundedException(EntityNotFoundedException e) {
        ExceptionsResponseDTO responseDTO = new ExceptionsResponseDTO(HttpStatus.NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(responseDTO, HttpStatus.NOT_FOUND);
    }

    // Gère l'exception lorsque l'entité existe déjà
    @ExceptionHandler(EntityAlreadyExists.class)
    public ResponseEntity<ExceptionsResponseDTO> entityAlreadyExists(EntityAlreadyExists e) {
        ExceptionsResponseDTO responseDTO = new ExceptionsResponseDTO(HttpStatus.CONFLICT, e.getMessage());
        return new ResponseEntity<>(responseDTO, HttpStatus.CONFLICT);
    }
}
