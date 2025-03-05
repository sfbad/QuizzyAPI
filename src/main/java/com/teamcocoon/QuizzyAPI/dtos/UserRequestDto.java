package com.teamcocoon.QuizzyAPI.dtos;
import jakarta.validation.constraints.NotBlank;

public record UserRequestDto(@NotBlank(message = " le username doit pas etre vide")
                             String username) {
}
