package br.com.mentorhub.mentorships.api.dto;

import jakarta.validation.constraints.Size;

public record CompleteSessionRequest(
        @Size(max = 2000, message = "Observações devem ter no máximo 2000 caracteres")
        String notes
) {
}
