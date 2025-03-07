package com.teamcocoon.QuizzyAPI.dtos;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UserRequestDto(
        @Schema(description = "Nom de l'utilisateur", example = "Jean")
        @NotBlank(message = " le username doit pas etre vide")
                             String username) {
}
