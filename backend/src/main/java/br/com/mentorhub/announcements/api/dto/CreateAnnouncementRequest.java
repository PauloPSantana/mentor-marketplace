package br.com.mentorhub.announcements.api.dto;

import br.com.mentorhub.announcements.domain.AnnouncementAudienceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateAnnouncementRequest(
        @Size(max = 180, message = "Título deve ter no máximo 180 caracteres")
        String title,
        @NotBlank(message = "O comunicado é obrigatório")
        @Size(max = 5000, message = "Comunicado deve ter no máximo 5000 caracteres")
        String body,
        @NotNull(message = "Informe o público do comunicado")
        AnnouncementAudienceType audienceType,
        List<UUID> recipientUserIds
) {
}
