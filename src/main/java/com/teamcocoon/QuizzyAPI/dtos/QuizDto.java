package com.teamcocoon.QuizzyAPI.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import jakarta.validation.constraints.*;

import java.util.Map;

@Builder
public record QuizDto (@NotNull(message = "L'ID ne peut pas être nul")
                        @Schema(description = "ID du quiz", example = "1")
                       Long id,

                       @Schema(description = "Titre du quiz", example = "Quiz sur les animaux")
                       @NotBlank(message = "Le nom est obligatoire")
                       String title,

                          @Schema(description = "Description du quiz", example = "Quiz sur les animaux")
                       String description,


                       Map<String, String> _links){
}
