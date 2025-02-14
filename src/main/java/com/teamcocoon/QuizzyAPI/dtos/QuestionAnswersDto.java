package com.teamcocoon.QuizzyAPI.dtos;

import lombok.Builder;

import java.util.List;

@Builder
public record QuestionAnswersDto(String title, List<AnswersDTO> answers) {
}
