package com.teamcocoon.QuizzyAPI.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AnswersDTO(
        @NotBlank(message = " le champ title doit pas etre vide")
        String title,

        @NotNull(message = " le champ isCorrect doit pas etre vide")
        boolean isCorrect) {
}
