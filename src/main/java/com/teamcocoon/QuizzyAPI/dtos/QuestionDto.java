package com.teamcocoon.QuizzyAPI.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record QuestionDto (
        @Schema(description = "texte de la question", example = "Quelle est la capitale de la France ?")
        @NotBlank(message = "le titre ne doit pas etre vide")
        String title){
}
