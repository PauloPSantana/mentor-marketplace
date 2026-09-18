package br.com.mentorhub.studyplans.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertStudyPlanRequest(
        @NotBlank(message = "Título do plano de estudos é obrigatório")
        @Size(max = 180, message = "Título deve ter no máximo 180 caracteres")
        String title,
        @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
        String description
) {
}
