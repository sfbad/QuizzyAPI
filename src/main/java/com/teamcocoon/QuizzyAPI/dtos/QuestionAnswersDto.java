package com.teamcocoon.QuizzyAPI.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Builder;

import java.util.List;

import jakarta.validation.constraints.*;


@Builder
public record QuestionAnswersDto(
        @Schema(description = "Titre de la question", example = "Quel est le nom de la capitale de la France ?")
        @NotBlank(message = "Le titre doit etre renseigné.")
        String title,

        @Valid
        List<AnswersDTO> answers) {
}
