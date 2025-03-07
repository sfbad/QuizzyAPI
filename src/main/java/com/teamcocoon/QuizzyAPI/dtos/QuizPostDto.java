package com.teamcocoon.QuizzyAPI.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record QuizPostDto(@Schema(description = "Titre du quiz", example = "Quiz sur les animaux")
                       @NotBlank(message = "Le nom est obligatoire")
                       String title,

                          @Schema(description = "Description du quiz", example = "Quiz sur les animaux")
                       String description){
}
