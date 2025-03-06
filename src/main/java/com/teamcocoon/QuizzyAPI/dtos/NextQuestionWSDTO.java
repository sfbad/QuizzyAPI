package com.teamcocoon.QuizzyAPI.dtos;

import lombok.Builder;

import java.util.List;
@Builder
public record NextQuestionWSDTO(QuestionTitleDTO question, List<ResponseTitleDTO> answers) {
}
