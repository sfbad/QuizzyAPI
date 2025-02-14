package com.teamcocoon.QuizzyAPI.dtos;

import lombok.Builder;

import java.util.List;

@Builder
public record ListQuestionsDto(String title, String description, List<QuestionAnswersDto> questions) {
}
