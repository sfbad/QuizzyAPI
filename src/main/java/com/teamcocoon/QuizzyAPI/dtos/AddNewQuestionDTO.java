package com.teamcocoon.QuizzyAPI.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddNewQuestionDTO(@NotNull(message = "Le titre doit etre renseigné.")
                                String title,
                                List<AnswersDTO> answers) {
}
