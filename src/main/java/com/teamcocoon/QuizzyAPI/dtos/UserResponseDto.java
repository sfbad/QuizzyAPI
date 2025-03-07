package com.teamcocoon.QuizzyAPI.dtos;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record UserResponseDto(
        @Schema(description = "ID de l'utilisateur", example = "1")
        @NotNull(message = "L'ID ne peut pas être nul")
        String uid,

        @Schema(description = "Email de l'utilisateur", example = "toto@gmail.com")
        @Email(message = "L'email doit être valide")
        String email,

        @Schema(description = "Nom de l'utilisateur", example = "toto")
        @NotBlank(message = "Le nom est obligatoire")
        String username
) {}