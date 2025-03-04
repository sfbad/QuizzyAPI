package com.teamcocoon.QuizzyAPI.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;
@Builder
public record QuizResponseDto(
        @NotBlank(message = "Le titre doit etre renseigné.")
        String title,

        @NotBlank(message = "La description doit etre renseigné.")
        String description,

        @Valid
        List<QuestionDto> questions){
}
