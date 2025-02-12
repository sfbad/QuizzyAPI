package com.teamcocoon.QuizzyAPI.dtos;

import com.teamcocoon.QuizzyAPI.model.Question;
import lombok.Builder;

import java.util.List;
@Builder
public record QuizResponseDto(String title, String description, List<QuestionDto> questions){
}
