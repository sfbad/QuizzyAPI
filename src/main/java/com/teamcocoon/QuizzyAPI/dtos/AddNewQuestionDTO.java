package com.teamcocoon.QuizzyAPI.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddNewQuestionDTO(
        @Schema(description = "texte de la question", example = "Quelle est la capitale de la France ?")
        @NotNull(message = "Le titre doit etre renseigné.")
                                String title,

                                @Valid
                                List<AnswersDTO> answers) {
}
