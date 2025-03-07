package com.teamcocoon.QuizzyAPI.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record PatchQuizTitleRequestDTO(
        @Schema(description = "opération à effectuer", example = "replace")
        @NotBlank(message = " le champ op doit pas etre vide")
        String op,

        @Schema(description = "path du champ à modifier", example = "/title")
        @NotBlank(message = " le champ path doit pas etre vide")
        String path,

        @Schema(description = "valeur à mettre à jour", example = "Nouveau titre")
        @NotBlank(message = " le champ value doit pas etre vide")
        String value) {
}
