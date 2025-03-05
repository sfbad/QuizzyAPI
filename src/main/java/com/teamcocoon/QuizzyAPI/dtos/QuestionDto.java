package com.teamcocoon.QuizzyAPI.dtos;

import jakarta.validation.constraints.NotBlank;

public record QuestionDto (
        @NotBlank(message = "le titre ne doit pas etre vide")
        String title){
}
