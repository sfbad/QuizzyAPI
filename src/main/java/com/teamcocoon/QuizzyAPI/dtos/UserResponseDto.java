package com.teamcocoon.QuizzyAPI.dtos;
import jakarta.validation.constraints.*;

public record UserResponseDto(
        @NotNull(message = "L'ID ne peut pas être nul")
        String uid,

        @Email(message = "L'email doit être valide")
        String email,

        @NotBlank(message = "Le nom est obligatoire")
        String username
) {}