package com.teamcocoon.QuizzyAPI.dtos;

import lombok.Builder;
import jakarta.validation.constraints.*;

import java.util.Map;

@Builder
public record QuizDto (@NotNull(message = "L'ID ne peut pas être nul")
                       Long id,

                       @NotBlank(message = "Le nom est obligatoire")
                       String title,

                       String description,
                       Map<String, String> _links){
}
