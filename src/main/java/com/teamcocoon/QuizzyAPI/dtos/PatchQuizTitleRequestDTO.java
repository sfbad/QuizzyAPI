package com.teamcocoon.QuizzyAPI.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record PatchQuizTitleRequestDTO(
        @NotBlank(message = " le champ op doit pas etre vide")
        String op,

        @NotBlank(message = " le champ path doit pas etre vide")
        String path,

        @NotBlank(message = " le champ value doit pas etre vide")
        String value) {
}
