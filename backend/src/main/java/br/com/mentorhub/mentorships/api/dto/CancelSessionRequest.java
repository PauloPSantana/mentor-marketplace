package br.com.mentorhub.mentorships.api.dto;

import jakarta.validation.constraints.Size;

public record CancelSessionRequest(
        @Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres")
        String reason
) {
}
