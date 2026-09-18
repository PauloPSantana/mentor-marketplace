package br.com.mentorhub.mentorships.api.dto;

import br.com.mentorhub.mentorships.domain.MeetingProvider;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateSessionRequest(
        @NotNull(message = "Horário da sessão é obrigatório")
        Instant scheduledAt,
        @NotNull(message = "Duração é obrigatória")
        @Min(value = 1, message = "Duração da sessão deve ser positiva")
        @Max(value = 240, message = "Duração da sessão deve ser de no máximo 240 minutos")
        Integer durationMinutes,
        @Size(max = 500, message = "Link da reunião deve ter no máximo 500 caracteres")
        String meetingUrl,
        @Size(max = 2000, message = "Observações devem ter no máximo 2000 caracteres")
        String notes,
        Boolean recordingConsent,
        MeetingProvider meetingProvider,
        @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
        String title
) {
}
