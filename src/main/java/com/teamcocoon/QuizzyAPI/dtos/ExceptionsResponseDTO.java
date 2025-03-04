package com.teamcocoon.QuizzyAPI.dtos;

import org.springframework.http.HttpStatus;

public record ExceptionsResponseDTO(HttpStatus status,String message) {
    public int statusCode() {
        return status.value();
    }
}
