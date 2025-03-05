package com.teamcocoon.QuizzyAPI.dtos;

import lombok.Builder;

@Builder
public record StatusUpdateDTO(String status, int participants) {
}
