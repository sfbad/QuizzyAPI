package com.teamcocoon.QuizzyAPI.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ListQuizResponseDto(

        @Valid
        List<QuizDto> data) {
}
