package com.teamcocoon.QuizzyAPI.dtos;

import java.util.List;

public record ListQuestionsDto(String title, List<QuestionAnswersDto> questions) {
}
