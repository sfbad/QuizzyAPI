package com.teamcocoon.QuizzyAPI.dtos;

import lombok.Builder;

@Builder
public record PatchQuizTitleRequestDTO(String op, String path, String value) {
}
