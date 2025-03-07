package com.teamcocoon.QuizzyAPI.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AnswersDTO(
        @Schema(description = "texte de la reponse", example = "Paris")
        @NotBlank(message = " le champ title doit pas etre vide")
        String title,

        @Schema(description = "indique si la reponse est correcte", example = "true")
        @NotNull(message = " le champ isCorrect doit pas etre vide")
        boolean isCorrect) {
}
