package br.com.mentorhub.mentorships.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record RescheduleSessionRequest(
        @NotNull(message = "Horário da sessão é obrigatório")
        Instant scheduledAt,
        @NotNull(message = "Duração é obrigatória")
        @Min(value = 1, message = "Duração da sessão deve ser positiva")
        @Max(value = 240, message = "Duração da sessão deve ser de no máximo 240 minutos")
        Integer durationMinutes
) {
}
