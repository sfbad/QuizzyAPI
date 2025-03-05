package com.teamcocoon.QuizzyAPI.dtos;

import lombok.Builder;
import jakarta.validation.constraints.*;

@Builder
public record QuizDto (@NotNull(message = "L'ID ne peut pas être nul")
                       Long id,

                       @NotBlank(message = "Le nom est obligatoire")
                       String title){
}
