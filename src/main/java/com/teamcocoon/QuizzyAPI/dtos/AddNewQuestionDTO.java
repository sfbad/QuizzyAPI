package com.teamcocoon.QuizzyAPI.dtos;

import java.util.List;

public record AddNewQuestionDTO(String title, List<AnswersDTO> answers) {
}
