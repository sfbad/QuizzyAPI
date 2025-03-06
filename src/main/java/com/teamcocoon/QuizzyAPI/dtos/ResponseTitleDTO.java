package com.teamcocoon.QuizzyAPI.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ResponseTitleDTO(
        @NotBlank(message = " le champ title doit pas etre vide")
        String title) {
}
