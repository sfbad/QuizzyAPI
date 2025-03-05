package com.teamcocoon.QuizzyAPI.dtos;

import jakarta.validation.Valid;
import lombok.Builder;

import java.util.List;

import jakarta.validation.constraints.*;


@Builder
public record QuestionAnswersDto(
        @NotBlank(message = "Le titre doit etre renseigné.")
        String title,

        @Valid
        List<AnswersDTO> answers) {
}
