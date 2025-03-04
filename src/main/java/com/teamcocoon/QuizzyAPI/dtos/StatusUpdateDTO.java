package com.teamcocoon.QuizzyAPI.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateDTO(@NotBlank(message = "Le status est obligatoire")
                              String status,
                              @NotNull(message = "Le nombre de participants doit être renseigné")
                              int participants) {
}
