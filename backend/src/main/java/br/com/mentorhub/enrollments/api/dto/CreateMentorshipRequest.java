package br.com.mentorhub.enrollments.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateMentorshipRequest(
        @NotNull(message = "Mentor é obrigatório")
        UUID mentorId,
        UUID mentoringServiceId,
        @Size(max = 2000, message = "Mensagem deve ter no máximo 2000 caracteres")
        String message
) {
}
