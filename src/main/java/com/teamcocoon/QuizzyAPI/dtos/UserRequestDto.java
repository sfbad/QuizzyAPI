package com.teamcocoon.QuizzyAPI.dtos;
import jakarta.validation.constraints.NotBlank;

public record UserRequestDto(@NotBlank String username) {
}
