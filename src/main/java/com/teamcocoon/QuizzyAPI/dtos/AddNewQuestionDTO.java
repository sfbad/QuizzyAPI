package com.teamcocoon.QuizzyAPI.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddNewQuestionDTO(@NotNull(message = "Le titre doit etre renseigné.")
                                String title,

                                @Valid
                                List<AnswersDTO> answers) {
}
