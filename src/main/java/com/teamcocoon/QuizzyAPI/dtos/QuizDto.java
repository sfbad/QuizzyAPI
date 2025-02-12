package com.teamcocoon.QuizzyAPI.dtos;

import lombok.Builder;

@Builder
public record QuizDto (Long id, String title){
}
