package com.teamcocoon.QuizzyAPI.dtos;

import java.util.List;

public record QuestionAnswersDto(String title, List<AnswersDTO> answers) {
}
