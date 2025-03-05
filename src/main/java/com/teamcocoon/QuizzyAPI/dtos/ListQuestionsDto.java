package com.teamcocoon.QuizzyAPI.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record ListQuestionsDto(
        @NotBlank(message = "Le titre doit etre renseigné.")
        String title,

        @NotBlank(message = "La description doit etre renseigné.")
        String description,

        @Valid
        List<QuestionAnswersDto> questions) {
}
