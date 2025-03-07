package com.teamcocoon.QuizzyAPI.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record ListQuestionsDto(
        @Schema(description = "Titre du quiz", example = "Quiz sur les animaux")
        @NotBlank(message = "Le titre doit etre renseigné.")
        String title,

        @Schema(description = "Description du quiz", example = "Quiz sur les animaux")
        @NotBlank(message = "La description doit etre renseigné.")
        String description,

        @Valid
        List<QuestionAnswersDto> questions) {
}
