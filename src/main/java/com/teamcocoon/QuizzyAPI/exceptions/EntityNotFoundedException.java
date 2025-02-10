package com.teamcocoon.QuizzyAPI.exceptions;

public class EntityNotFoundedException extends RuntimeException {
    public EntityNotFoundedException(String message) {
        super(message);
    }
}
