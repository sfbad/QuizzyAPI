package com.teamcocoon.QuizzyAPI.dtos;

import jakarta.validation.constraints.NotBlank;

public record QuestionTitleDTO(
        @NotBlank(message = " le champ title doit pas etre vide")
        String title) {
}
