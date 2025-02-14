package com.teamcocoon.QuizzyAPI.dtos;

import lombok.Builder;

@Builder
public record AnswersDTO(String title, boolean isCorrect) {
}
