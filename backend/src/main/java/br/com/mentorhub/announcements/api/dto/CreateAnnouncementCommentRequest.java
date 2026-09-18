package br.com.mentorhub.announcements.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAnnouncementCommentRequest(
        @NotBlank(message = "O comentário é obrigatório")
        @Size(max = 1000, message = "Comentário deve ter no máximo 1000 caracteres")
        String content
) {
}
